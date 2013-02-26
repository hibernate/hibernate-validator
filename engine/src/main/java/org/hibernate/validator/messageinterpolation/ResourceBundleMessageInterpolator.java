/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.MessageInterpolator;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.LocalizedMessage;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import static org.hibernate.validator.internal.util.CollectionHelper.newConcurrentHashMap;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ResourceBundleMessageInterpolator implements MessageInterpolator {

	/**
	 * The name of the default message bundle.
	 */
	public static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

	/**
	 * The name of the user-provided message bundle as defined in the specification.
	 */
	public static final String USER_VALIDATION_MESSAGES = "ValidationMessages";

	/**
	 * Regular expression used to do locating message parameters.
	 */
	private static final Pattern MESSAGE_PARAMETER_PATTERN = Pattern.compile( "((\\\\*)\\{[^\\}]+?\\})" );

	/**
	 * Regular expression used to do locating message expressions.
	 */
	private static final Pattern MESSAGE_EXPRESSION_PATTERN = Pattern.compile( "((\\\\*)\\$?\\{[^\\}]+?\\})" );

	/**
	 * The default locale in the current JVM.
	 */
	private final Locale defaultLocale;

	/**
	 * Loads user-specified resource bundles.
	 */
	private final ResourceBundleLocator userResourceBundleLocator;

	/**
	 * Loads built-in resource bundles.
	 */
	private final ResourceBundleLocator defaultResourceBundleLocator;

	/**
	 * Step 1-3 of message interpolation can be cached. We do this in this map.
	 */
	private final ConcurrentMap<LocalizedMessage, String> resolvedMessages = newConcurrentHashMap();

	/**
	 * Flag indicating whether this interpolator should chance some of the interpolation steps.
	 */
	private final boolean cacheMessages;

	public ResourceBundleMessageInterpolator() {
		this( null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
		this( userResourceBundleLocator, true );
	}

	public ResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator, boolean cacheMessages) {
		defaultLocale = Locale.getDefault();

		if ( userResourceBundleLocator == null ) {
			this.userResourceBundleLocator = new PlatformResourceBundleLocator( USER_VALIDATION_MESSAGES );
		}
		else {
			this.userResourceBundleLocator = userResourceBundleLocator;
		}

		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator( DEFAULT_VALIDATION_MESSAGES );
		this.cacheMessages = cacheMessages;
	}

	@Override
	public String interpolate(String message, Context context) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparison has to be based on == and not equals though
		return interpolateMessage( message, context, defaultLocale );
	}

	@Override
	public String interpolate(String message, Context context, Locale locale) {
		return interpolateMessage( message, context, locale );
	}

	/**
	 * Runs the message interpolation according to algorithm specified in the Bean Validation specification.
	 * <br/>
	 * Note:
	 * <br/>
	 * Look-ups in user bundles is recursive whereas look-ups in default bundle are not!
	 *
	 * @param message the message to interpolate
	 * @param context the context for this interpolation
	 * @param locale the {@code Locale} to use for the resource bundle.
	 *
	 * @return the interpolated message.
	 */
	private String interpolateMessage(String message, Context context, Locale locale) {
		LocalizedMessage localisedMessage = new LocalizedMessage( message, locale );
		String resolvedMessage = null;

		if ( cacheMessages ) {
			resolvedMessage = resolvedMessages.get( localisedMessage );
		}

		// if the message is not already in the cache we have to run step 1-3 of the message resolution 
		if ( resolvedMessage == null ) {
			ResourceBundle userResourceBundle = userResourceBundleLocator
					.getResourceBundle( locale );
			ResourceBundle defaultResourceBundle = defaultResourceBundleLocator
					.getResourceBundle( locale );

			String userBundleResolvedMessage;
			resolvedMessage = message;
			boolean evaluatedDefaultBundleOnce = false;
			do {
				// search the user bundle recursive (step1)
				userBundleResolvedMessage = interpolateBundleMessage(
						resolvedMessage, userResourceBundle, locale, true
				);

				// exit condition - we have at least tried to validate against the default bundle and there was no
				// further replacements
				if ( evaluatedDefaultBundleOnce
						&& !hasReplacementTakenPlace( userBundleResolvedMessage, resolvedMessage ) ) {
					break;
				}

				// search the default bundle non recursive (step2)
				resolvedMessage = interpolateBundleMessage(
						userBundleResolvedMessage,
						defaultResourceBundle,
						locale,
						false
				);
				evaluatedDefaultBundleOnce = true;
			} while ( true );
		}

		// cache resolved message
		if ( cacheMessages ) {
			String cachedResolvedMessage = resolvedMessages.putIfAbsent( localisedMessage, resolvedMessage );
			if ( cachedResolvedMessage != null ) {
				resolvedMessage = cachedResolvedMessage;
			}
		}

		// resolve annotation attributes (step 4)
		resolvedMessage = interpolateExpression( resolvedMessage, MESSAGE_PARAMETER_PATTERN, context, locale );

		// resolve annotation attributes (step 5)
		resolvedMessage = interpolateExpression( resolvedMessage, MESSAGE_EXPRESSION_PATTERN, context, locale );

		// last but not least we have to take care of escaped literals
		resolvedMessage = resolvedMessage.replace( "\\{", "{" );
		resolvedMessage = resolvedMessage.replace( "\\}", "}" );
		resolvedMessage = resolvedMessage.replace( "\\\\", "\\" );
		resolvedMessage = resolvedMessage.replace( "\\$", "$" );

		return resolvedMessage;
	}

	private boolean hasReplacementTakenPlace(String origMessage, String newMessage) {
		return !origMessage.equals( newMessage );
	}

	private String interpolateBundleMessage(String message, ResourceBundle bundle, Locale locale, boolean recurse) {
		Matcher matcher = MESSAGE_PARAMETER_PATTERN.matcher( message );
		StringBuffer sb = new StringBuffer();
		String resolvedParameterValue;
		while ( matcher.find() ) {
			String parameter = matcher.group( 1 );
			resolvedParameterValue = resolveParameter(
					parameter, bundle, locale, recurse
			);

			matcher.appendReplacement( sb, Matcher.quoteReplacement( resolvedParameterValue ) );
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String interpolateExpression(String message, Pattern pattern, Context context, Locale locale) {
		Matcher matcher = pattern.matcher( message );
		StringBuffer sb = new StringBuffer();

		while ( matcher.find() ) {
			String match = matcher.group( 1 );
			InterpolationTerm expression = new InterpolationTerm( match, locale );
			if ( expression.needsEvaluation() ) {
				String resolvedExpression = expression.interpolate( context );
				resolvedExpression = Matcher.quoteReplacement( resolvedExpression );
				matcher.appendReplacement( sb, resolvedExpression );
			}
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String resolveParameter(String parameterName, ResourceBundle bundle, Locale locale, boolean recurse) {
		String parameterValue;
		try {
			if ( bundle != null ) {
				parameterValue = bundle.getString( removeCurlyBraces( parameterName ) );
				if ( recurse ) {
					parameterValue = interpolateBundleMessage( parameterValue, bundle, locale, recurse );
				}
			}
			else {
				parameterValue = parameterName;
			}
		}
		catch ( MissingResourceException e ) {
			// return parameter itself
			parameterValue = parameterName;
		}
		return parameterValue;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}
}
