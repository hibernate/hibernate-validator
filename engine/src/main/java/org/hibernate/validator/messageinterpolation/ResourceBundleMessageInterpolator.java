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

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.validation.MessageInterpolator;
import javax.xml.bind.ValidationException;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.LocalizedMessage;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ResourceBundleMessageInterpolator implements MessageInterpolator {
	private static final Log log = LoggerFactory.make();

	/**
	 * The default initial capacity for this cache.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 100;

	/**
	 * The default load factor for this cache.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this cache.
	 */
	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	/**
	 * The name of the default message bundle.
	 */
	private static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

	/**
	 * The name of the user-provided message bundle as defined in the specification.
	 */
	public static final String USER_VALIDATION_MESSAGES = "ValidationMessages";

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
	private final ConcurrentReferenceHashMap<LocalizedMessage, String> resolvedMessages;

	/**
	 * Step 4 of message interpolation replaces message parameters. The token list for message parameters is cached in this map.
	 */
	private final ConcurrentReferenceHashMap<String, List<Token>> tokenizedParameterMessages;

	/**
	 * Step 5 of message interpolation replaces EL expressions. The token list for EL expressions is cached in this map.
	 */
	private final ConcurrentReferenceHashMap<String, List<Token>> tokenizedELMessages;

	/**
	 * Flag indicating whether this interpolator should cache some of the interpolation steps.
	 */
	private final boolean cachingEnabled;

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
		this.cachingEnabled = cacheMessages;

		if ( cachingEnabled ) {
			this.resolvedMessages = new ConcurrentReferenceHashMap<LocalizedMessage, String>(
					DEFAULT_INITIAL_CAPACITY,
					DEFAULT_LOAD_FACTOR,
					DEFAULT_CONCURRENCY_LEVEL,
					SOFT,
					SOFT,
					EnumSet.noneOf( ConcurrentReferenceHashMap.Option.class )
			);
			this.tokenizedParameterMessages = new ConcurrentReferenceHashMap<String, List<Token>>(
					DEFAULT_INITIAL_CAPACITY,
					DEFAULT_LOAD_FACTOR,
					DEFAULT_CONCURRENCY_LEVEL,
					SOFT,
					SOFT,
					EnumSet.noneOf( ConcurrentReferenceHashMap.Option.class )
			);
			this.tokenizedELMessages = new ConcurrentReferenceHashMap<String, List<Token>>(
					DEFAULT_INITIAL_CAPACITY,
					DEFAULT_LOAD_FACTOR,
					DEFAULT_CONCURRENCY_LEVEL,
					SOFT,
					SOFT,
					EnumSet.noneOf( ConcurrentReferenceHashMap.Option.class )
			);
		}
		else {
			resolvedMessages = null;
			tokenizedParameterMessages = null;
			tokenizedELMessages = null;
		}

		// HV-793 - To fail eagerly in case we have no EL dependencies on the classpath we try to load the expression
		// factory
		try {
			ResourceBundleMessageInterpolator.class.getClassLoader().loadClass( "javax.el.ExpressionFactory" );
		}
		catch ( ClassNotFoundException e ) {
			throw log.getMissingELDependenciesException();
		}
	}

	@Override
	public String interpolate(String message, Context context) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparison has to be based on == and not equals though
		String interpolatedMessage = message;
		try {
			interpolatedMessage = interpolateMessage( message, context, defaultLocale );
		}
		catch ( MessageDescriptorFormatException e ) {
			log.warn( e.getMessage() );
		}
		return interpolatedMessage;
	}

	@Override
	public String interpolate(String message, Context context, Locale locale) {
		String interpolatedMessage = message;
		try {
			interpolatedMessage = interpolateMessage( message, context, locale );
		}
		catch ( ValidationException e ) {
			log.warn( e.getMessage() );
		}
		return interpolatedMessage;
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
	private String interpolateMessage(String message, Context context, Locale locale)
			throws MessageDescriptorFormatException {
		LocalizedMessage localisedMessage = new LocalizedMessage( message, locale );
		String resolvedMessage = null;

		if ( cachingEnabled ) {
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
		if ( cachingEnabled ) {
			String cachedResolvedMessage = resolvedMessages.putIfAbsent( localisedMessage, resolvedMessage );
			if ( cachedResolvedMessage != null ) {
				resolvedMessage = cachedResolvedMessage;
			}
		}

		// resolve parameter expressions (step 4)
		List<Token> tokens = null;
		if ( cachingEnabled ) {
			tokens = tokenizedParameterMessages.get( resolvedMessage );
		}
		if ( tokens == null ) {
			TokenCollector tokenCollector = new TokenCollector( resolvedMessage, InterpolationTermType.PARAMETER );
			tokens = tokenCollector.getTokenList();

			if ( cachingEnabled ) {
				tokenizedParameterMessages.putIfAbsent( resolvedMessage, tokens );
			}
		}
		resolvedMessage = interpolateExpression(
				new TokenIterator( tokens ),
				context,
				locale
		);

		// resolve EL expressions (step 5)
		tokens = null;
		if ( cachingEnabled ) {
			tokens = tokenizedELMessages.get( resolvedMessage );
		}
		if ( tokens == null ) {
			TokenCollector tokenCollector = new TokenCollector( resolvedMessage, InterpolationTermType.EL );
			tokens = tokenCollector.getTokenList();

			if ( cachingEnabled ) {
				tokenizedELMessages.putIfAbsent( resolvedMessage, tokens );
			}
		}
		resolvedMessage = interpolateExpression(
				new TokenIterator( tokens ),
				context,
				locale
		);

		// last but not least we have to take care of escaped literals
		resolvedMessage = replaceEscapedLiterals( resolvedMessage );

		return resolvedMessage;
	}

	private String replaceEscapedLiterals(String resolvedMessage) {
		resolvedMessage = resolvedMessage.replace( "\\{", "{" );
		resolvedMessage = resolvedMessage.replace( "\\}", "}" );
		resolvedMessage = resolvedMessage.replace( "\\\\", "\\" );
		resolvedMessage = resolvedMessage.replace( "\\$", "$" );
		return resolvedMessage;
	}

	private boolean hasReplacementTakenPlace(String origMessage, String newMessage) {
		return !origMessage.equals( newMessage );
	}

	private String interpolateBundleMessage(String message, ResourceBundle bundle, Locale locale, boolean recursive)
			throws MessageDescriptorFormatException {
		TokenCollector tokenCollector = new TokenCollector( message, InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );
		while ( tokenIterator.hasMoreInterpolationTerms() ) {
			String term = tokenIterator.nextInterpolationTerm();
			String resolvedParameterValue = resolveParameter(
					term, bundle, locale, recursive
			);
			tokenIterator.replaceCurrentInterpolationTerm( resolvedParameterValue );
		}
		return tokenIterator.getInterpolatedMessage();
	}

	private String interpolateExpression(TokenIterator tokenIterator, Context context, Locale locale)
			throws MessageDescriptorFormatException {
		while ( tokenIterator.hasMoreInterpolationTerms() ) {
			String term = tokenIterator.nextInterpolationTerm();

			InterpolationTerm expression = new InterpolationTerm( term, locale );
			String resolvedExpression = expression.interpolate( context );
			tokenIterator.replaceCurrentInterpolationTerm( resolvedExpression );
		}
		return tokenIterator.getInterpolatedMessage();
	}

	private String resolveParameter(String parameterName, ResourceBundle bundle, Locale locale, boolean recursive)
			throws MessageDescriptorFormatException {
		String parameterValue;
		try {
			if ( bundle != null ) {
				parameterValue = bundle.getString( removeCurlyBraces( parameterName ) );
				if ( recursive ) {
					parameterValue = interpolateBundleMessage( parameterValue, bundle, locale, recursive );
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
