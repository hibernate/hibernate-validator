// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.MessageInterpolator;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageInterpolator implements MessageInterpolator {
	private static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validation.ValidationMessages";
	private static final String USER_VALIDATION_MESSAGES = "ValidationMessages";
	private static final Logger log = LoggerFactory.make();

	/**
	 * Regular expression used to do message interpolation.
	 */
	private static final Pattern messageParameterPattern = Pattern.compile( "(\\{[\\w\\.]+\\})" );

	/**
	 * The default locale for the current user.
	 */
	private final Locale defaultLocale;

	/**
	 * User specified resource bundles hashed against their locale.
	 */
	private final Map<Locale, ResourceBundle> userBundlesMap = new ConcurrentHashMap<Locale, ResourceBundle>();

	/**
	 * Builtin resource bundles hashed against there locale.
	 */
	private final Map<Locale, ResourceBundle> defaultBundlesMap = new ConcurrentHashMap<Locale, ResourceBundle>();

	public ResourceBundleMessageInterpolator() {
		this( null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundle resourceBundle) {

		defaultLocale = Locale.getDefault();

		if ( resourceBundle == null ) {
			ResourceBundle bundle = getFileBasedResourceBundle( defaultLocale );
			if ( bundle != null ) {
				userBundlesMap.put( defaultLocale, bundle );
			}

		}
		else {
			userBundlesMap.put( defaultLocale, resourceBundle );
		}

		defaultBundlesMap.put( defaultLocale, ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES, defaultLocale ) );
	}

	/**
	 * {@inheritDoc}
	 */
	public String interpolate(String message, Context context) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparaison has to be based on == and not equals though
		return interpolateMessage( message, context.getConstraintDescriptor().getAttributes(), defaultLocale );
	}

	/**
	 * {@inheritDoc}
	 */
	public String interpolate(String message, Context context, Locale locale) {
		return interpolateMessage( message, context.getConstraintDescriptor().getAttributes(), locale );
	}

	/**
	 * Runs the message interpolation according to alogrithm specified in JSR 303.
	 * <br/>
	 * Note:
	 * <br/>
	 * Lookups in user bundles is recursive whereas lookups in default bundle are not!
	 *
	 * @param message the message to interpolate
	 * @param annotationParameters the parameters of the annotation for which to interpolate this message
	 * @param locale the <code>Locale</code> to use for the resource bundle.
	 *
	 * @return the interpolated message.
	 */
	private String interpolateMessage(String message, Map<String, Object> annotationParameters, Locale locale) {
		ResourceBundle userResourceBundle = findUserResourceBundle( locale );
		ResourceBundle defaultResourceBundle = findDefaultResourceBundle( locale );

		String userBundleResolvedMessage;
		String resolvedMessage = message;
		boolean evaluatedDefaultBundleOnce = false;
		do {
			// search the user bundle recursive (step1)
			userBundleResolvedMessage = replaceVariables(
					resolvedMessage, userResourceBundle, locale, true
			);

			// exit condition - we have at least tried to vaidate against the default bundle and there was no
			// further replacements
			if ( evaluatedDefaultBundleOnce
					&& !hasReplacementTakenPlace( userBundleResolvedMessage, resolvedMessage ) ) {
				break;
			}

			// search the default bundle non recursive (step2)
			resolvedMessage = replaceVariables( userBundleResolvedMessage, defaultResourceBundle, locale, false );

			evaluatedDefaultBundleOnce = true;
		} while ( true );

		// resolve annotation attributes (step 4)
		resolvedMessage = replaceAnnotationAttributes( resolvedMessage, annotationParameters );

		// last but not least we have to take care of escaped literals
		resolvedMessage = resolvedMessage.replace( "\\{", "{" );
		resolvedMessage = resolvedMessage.replace( "\\}", "}" );
		resolvedMessage = resolvedMessage.replace( "\\\\", "\\" );
		return resolvedMessage;
	}

	private boolean hasReplacementTakenPlace(String origMessage, String newMessage) {
		return !origMessage.equals( newMessage );
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found, search validator (this) classloader.
	 *
	 * @param locale The locale of the bundle to load.
	 *
	 * @return the resource bundle or <code>null</code> if none is found.
	 */
	private ResourceBundle getFileBasedResourceBundle(Locale locale) {
		ResourceBundle rb = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader != null ) {
			rb = loadBundle( classLoader, locale, USER_VALIDATION_MESSAGES + " not found by thread local classloader" );
		}
		if ( rb == null ) {
			rb = loadBundle(
					this.getClass().getClassLoader(),
					locale,
					USER_VALIDATION_MESSAGES + " not found by validator classloader"
			);
		}
		if ( log.isDebugEnabled() ) {
			if ( rb != null ) {
				log.debug( USER_VALIDATION_MESSAGES + " found" );
			}
			else {
				log.debug( USER_VALIDATION_MESSAGES + " not found. Delegating to " + DEFAULT_VALIDATION_MESSAGES );
			}
		}
		return rb;
	}

	private ResourceBundle loadBundle(ClassLoader classLoader, Locale locale, String message) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle( USER_VALIDATION_MESSAGES, locale, classLoader );
		}
		catch ( MissingResourceException e ) {
			log.trace( message );
		}
		return rb;
	}

	private String replaceVariables(String message, ResourceBundle bundle, Locale locale, boolean recurse) {
		Matcher matcher = messageParameterPattern.matcher( message );
		StringBuffer sb = new StringBuffer();
		String resolvedParameterValue;
		while ( matcher.find() ) {
			String parameter = matcher.group( 1 );
			resolvedParameterValue = resolveParameter(
					parameter, bundle, locale, recurse
			);

			matcher.appendReplacement( sb, escapeMetaCharacters( resolvedParameterValue ) );
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String replaceAnnotationAttributes(String message, Map<String, Object> annotationParameters) {
		Matcher matcher = messageParameterPattern.matcher( message );
		StringBuffer sb = new StringBuffer();
		while ( matcher.find() ) {
			String resolvedParameterValue;
			String parameter = matcher.group( 1 );
			Object variable = annotationParameters.get( removeCurlyBrace( parameter ) );
			if ( variable != null ) {
				resolvedParameterValue = escapeMetaCharacters( variable.toString() );
			}
			else {
				resolvedParameterValue = message;
			}
			matcher.appendReplacement( sb, resolvedParameterValue );
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String resolveParameter(String parameterName, ResourceBundle bundle, Locale locale, boolean recurse) {
		String parameterValue;
		try {
			if ( bundle != null ) {
				parameterValue = bundle.getString( removeCurlyBrace( parameterName ) );
				if ( recurse ) {
					parameterValue = replaceVariables( parameterValue, bundle, locale, recurse );
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

	private String removeCurlyBrace(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}

	private ResourceBundle findDefaultResourceBundle(Locale locale) {
		if ( defaultBundlesMap.containsKey( locale ) ) {
			return defaultBundlesMap.get( locale );
		}

		ResourceBundle bundle = ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES, locale );
		defaultBundlesMap.put( locale, bundle );
		return bundle;
	}

	private ResourceBundle findUserResourceBundle(Locale locale) {
		if ( userBundlesMap.containsKey( locale ) ) {
			return userBundlesMap.get( locale );
		}

		ResourceBundle bundle = getFileBasedResourceBundle( locale );
		if ( bundle != null ) {
			userBundlesMap.put( locale, bundle );
		}
		return bundle;
	}

	/**
	 * @param s The string in which to replace the meta characters '$' and '\'.
	 *
	 * @return A string where meta characters relevant for {@link Matcher#appendReplacement} are escaped.
	 */
	private String escapeMetaCharacters(String s) {
		String escapedString = s.replace( "\\", "\\\\" );
		escapedString = escapedString.replace( "$", "\\$" );
		return escapedString;
	}
}
