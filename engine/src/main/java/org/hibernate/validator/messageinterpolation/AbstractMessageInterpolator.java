/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.MessageInterpolator;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.engine.PredefinedScopeValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolverContext;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.LocalizedMessage;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Adam Stawicki
 * @author Marko Bekhta
 * @author Guillaume Smet
 *
 * @since 5.2
 */
public abstract class AbstractMessageInterpolator implements MessageInterpolator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

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
	public static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";

	/**
	 * The name of the user-provided message bundle as defined in the specification.
	 */
	public static final String USER_VALIDATION_MESSAGES = "ValidationMessages";

	/**
	 * Default name of the message bundle defined by a constraint definition contributor.
	 *
	 * @since 5.2
	 */
	public static final String CONTRIBUTOR_VALIDATION_MESSAGES = "ContributorValidationMessages";

	/**
	 * The locale resolver.
	 */
	private final LocaleResolver localeResolver;

	/**
	 * The execution context of the locale resolver.
	 */
	private final LocaleResolverContext localeResolverContext;

	/**
	 * Loads user-specified resource bundles.
	 */
	private final ResourceBundleLocator userResourceBundleLocator;

	/**
	 * Loads built-in resource bundles.
	 */
	private final ResourceBundleLocator defaultResourceBundleLocator;

	/**
	 * Loads contributed resource bundles.
	 */
	private final ResourceBundleLocator contributorResourceBundleLocator;

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

	private static final Pattern LEFT_BRACE = Pattern.compile( "\\{", Pattern.LITERAL );
	private static final Pattern RIGHT_BRACE = Pattern.compile( "\\}", Pattern.LITERAL );
	private static final Pattern SLASH = Pattern.compile( "\\\\", Pattern.LITERAL );
	private static final Pattern DOLLAR = Pattern.compile( "\\$", Pattern.LITERAL );

	/**
	 * {@code MessageInterpolator} using the default resource bundle locators.
	 */
	public AbstractMessageInterpolator() {
		this( Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	/**
	 * {@code MessageInterpolator} taking a resource bundle locator.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 */
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
		this( userResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	/**
	 * {@code MessageInterpolator} taking two resource bundle locators.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 * @param contributorResourceBundleLocator {@code ResourceBundleLocator} used to load resource bundle of constraint contributor
	 * @since 5.2
	 */
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(), false );
	}

	/**
	 * {@code MessageInterpolator} taking two resource bundle locators.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 * @param contributorResourceBundleLocator {@code ResourceBundleLocator} used to load resource bundle of constraint contributor
	 * @param cacheMessages Whether resolved messages should be cached or not.
	 * @since 5.2
	 */
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			boolean cacheMessages) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, Collections.emptySet(), Locale.getDefault(), new DefaultLocaleResolver(),
				cacheMessages );
	}

	/**
	 * {@code MessageInterpolator} using the default resource bundle locators.
	 *
	 * @param locales the set of locales to initialize at bootstrap
	 * @param defaultLocale the default locale
	 * @param localeResolver the locale resolver
	 * @param preloadResourceBundles if the resource bundled should be initialized at initialization time, this is useful in the case of a
	 * {@link PredefinedScopeValidatorFactoryImpl}
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AbstractMessageInterpolator(Set<Locale> locales, Locale defaultLocale, LocaleResolver localeResolver, boolean preloadResourceBundles) {
		this( null, locales, defaultLocale, localeResolver, preloadResourceBundles );
	}

	/**
	 * {@code MessageInterpolator} taking a resource bundle locator.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 * @param locales the set of locales to initialize at bootstrap
	 * @param defaultLocale the default locale
	 * @param localeResolver the locale resolver
	 * @param preloadResourceBundles if the resource bundled should be initialized at initialization time, this is useful in the case of a
	 * {@link PredefinedScopeValidatorFactoryImpl}
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles) {
		this( userResourceBundleLocator, null, locales, defaultLocale, localeResolver, preloadResourceBundles );
	}

	/**
	 * {@code MessageInterpolator} taking two resource bundle locators.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 * @param contributorResourceBundleLocator {@code ResourceBundleLocator} used to load resource bundle of constraint contributor
	 * @param localesToInitialize the set of locales to initialize at bootstrap
	 * @param defaultLocale the default locale
	 * @param localeResolver the locale resolver
	 * @param preloadResourceBundles if the resource bundled should be initialized at initialization time, this is useful in the case of a
	 * {@link PredefinedScopeValidatorFactoryImpl}
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			Set<Locale> localesToInitialize,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles) {
		this( userResourceBundleLocator, contributorResourceBundleLocator, localesToInitialize, defaultLocale, localeResolver, preloadResourceBundles, true );
	}

	/**
	 * {@code MessageInterpolator} taking two resource bundle locators.
	 *
	 * @param userResourceBundleLocator {@code ResourceBundleLocator} used to load user provided resource bundle
	 * @param contributorResourceBundleLocator {@code ResourceBundleLocator} used to load resource bundle of constraint
	 * contributor
	 * @param locales the set of locales to initialize at bootstrap
	 * @param defaultLocale the default locale
	 * @param cacheMessages whether resolved messages should be cached or not
	 * @param localeResolver the locale resolver
	 * @param preloadResourceBundles if the resource bundled should be initialized at initialization time, this is useful in the case of a
	 * {@link PredefinedScopeValidatorFactoryImpl}
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public AbstractMessageInterpolator(ResourceBundleLocator userResourceBundleLocator,
			ResourceBundleLocator contributorResourceBundleLocator,
			Set<Locale> locales,
			Locale defaultLocale,
			LocaleResolver localeResolver,
			boolean preloadResourceBundles,
			boolean cacheMessages) {
		Contracts.assertNotNull( locales, MESSAGES.parameterMustNotBeNull( "localesToInitialize" ) );
		Contracts.assertNotNull( defaultLocale, MESSAGES.parameterMustNotBeNull( "defaultLocale" ) );
		Contracts.assertNotNull( localeResolver, MESSAGES.parameterMustNotBeNull( "localeResolver" ) );

		Set<Locale> allLocales = CollectionHelper.toImmutableSet( getAllLocales( locales, defaultLocale ) );

		this.localeResolverContext = new DefaultLocaleResolverContext( allLocales, defaultLocale );
		this.localeResolver = localeResolver;

		Set<Locale> allLocalesToInitialize = preloadResourceBundles ? allLocales : Collections.emptySet();

		if ( userResourceBundleLocator == null ) {
			this.userResourceBundleLocator = new PlatformResourceBundleLocator( USER_VALIDATION_MESSAGES,
					allLocalesToInitialize );
		}
		else {
			this.userResourceBundleLocator = userResourceBundleLocator;
		}

		if ( contributorResourceBundleLocator == null ) {
			this.contributorResourceBundleLocator = new PlatformResourceBundleLocator(
					CONTRIBUTOR_VALIDATION_MESSAGES,
					allLocalesToInitialize,
					null,
					true
			);
		}
		else {
			this.contributorResourceBundleLocator = contributorResourceBundleLocator;
		}

		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator( DEFAULT_VALIDATION_MESSAGES, allLocalesToInitialize );

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
	}

	@Override
	public String interpolate(String message, Context context) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparison has to be based on == and not equals though
		String interpolatedMessage = message;
		try {
			interpolatedMessage = interpolateMessage( message, context, localeResolver.resolve( localeResolverContext ) );
		}
		catch (MessageDescriptorFormatException e) {
			LOG.warn( e.getMessage() );
		}
		return interpolatedMessage;
	}

	@Override
	public String interpolate(String message, Context context, Locale locale) {
		String interpolatedMessage = message;
		try {
			interpolatedMessage = interpolateMessage( message, context, locale );
		}
		catch (MessageDescriptorFormatException e) {
			LOG.warn( e.getMessage() );
		}
		return interpolatedMessage;
	}

	private Set<Locale> getAllLocales(Set<Locale> localesToInitialize, Locale defaultLocale) {
		if ( localesToInitialize.contains( defaultLocale ) ) {
			return localesToInitialize;
		}

		Set<Locale> allLocales = new HashSet<>( localesToInitialize.size() + 1 );
		allLocales.addAll( localesToInitialize );
		allLocales.add( defaultLocale );
		return allLocales;
	}

	/**
	 * Runs the message interpolation according to algorithm specified in the Bean Validation specification.
	 * <p>
	 * Note:
	 * <p>
	 * Look-ups in user bundles is recursive whereas look-ups in default bundle are not!
	 *
	 * @param message the message to interpolate
	 * @param context the context for this interpolation
	 * @param locale the {@code Locale} to use for the resource bundle.
	 *
	 * @return the interpolated message.
	 */
	private String interpolateMessage(String message, Context context, Locale locale) throws MessageDescriptorFormatException {
		// if the message does not contain any message parameter, we can ignore the next steps and just return
		// the unescaped message. It avoids storing the message in the cache and a cache lookup.
		if ( message.indexOf( '{' ) < 0 ) {
			return replaceEscapedLiterals( message );
		}

		String resolvedMessage = null;

		// either retrieve message from cache, or if message is not yet there or caching is disabled,
		// perform message resolution algorithm (step 1)
		if ( cachingEnabled ) {
			resolvedMessage = resolvedMessages.computeIfAbsent( new LocalizedMessage( message, locale ), lm -> resolveMessage( message, locale ) );
		}
		else {
			resolvedMessage = resolveMessage( message, locale );
		}

		// there's no need for steps 2-3 unless there's `{param}`/`${expr}` in the message
		if ( resolvedMessage.indexOf( '{' ) > -1 ) {
			// resolve parameter expressions (step 2)
			resolvedMessage = interpolateExpression(
					new TokenIterator( getParameterTokens( resolvedMessage, tokenizedParameterMessages, InterpolationTermType.PARAMETER ) ),
					context,
					locale
			);

			// resolve EL expressions (step 3)
			// in the standard Hibernate Validator execution flow, the context is always an instance of
			// HibernateMessageInterpolatorContext
			// but it can be a spec Context in the Jakarta Bean Validation TCK.
			if ( !( context instanceof HibernateMessageInterpolatorContext )
					|| ( (HibernateMessageInterpolatorContext) context ).getExpressionLanguageFeatureLevel() != ExpressionLanguageFeatureLevel.NONE ) {
				resolvedMessage = interpolateExpression(
						new TokenIterator( getParameterTokens( resolvedMessage, tokenizedELMessages, InterpolationTermType.EL ) ),
						context,
						locale );
			}
		}

		// last but not least we have to take care of escaped literals
		resolvedMessage = replaceEscapedLiterals( resolvedMessage );

		return resolvedMessage;
	}

	private List<Token> getParameterTokens(String resolvedMessage, ConcurrentReferenceHashMap<String, List<Token>> cache, InterpolationTermType termType) {
		if ( cachingEnabled ) {
			return cache.computeIfAbsent(
					resolvedMessage,
					rm -> new TokenCollector( resolvedMessage, termType ).getTokenList()
			);
		}
		else {
			return new TokenCollector( resolvedMessage, termType ).getTokenList();
		}
	}

	private String resolveMessage(String message, Locale locale) {
		String resolvedMessage = message;

		ResourceBundle userResourceBundle = userResourceBundleLocator
				.getResourceBundle( locale );

		ResourceBundle constraintContributorResourceBundle = contributorResourceBundleLocator
				.getResourceBundle( locale );

		ResourceBundle defaultResourceBundle = defaultResourceBundleLocator
				.getResourceBundle( locale );

		String userBundleResolvedMessage;
		boolean evaluatedDefaultBundleOnce = false;
		do {
			// search the user bundle recursive (step 1.1)
			userBundleResolvedMessage = interpolateBundleMessage(
					resolvedMessage, userResourceBundle, locale, true
			);

			// search the constraint contributor bundle recursive (only if the user did not define a message)
			if ( !hasReplacementTakenPlace( userBundleResolvedMessage, resolvedMessage ) ) {
				userBundleResolvedMessage = interpolateBundleMessage(
						resolvedMessage, constraintContributorResourceBundle, locale, true
				);
			}

			// exit condition - we have at least tried to validate against the default bundle and there was no
			// further replacements
			if ( evaluatedDefaultBundleOnce && !hasReplacementTakenPlace( userBundleResolvedMessage, resolvedMessage ) ) {
				break;
			}

			// search the default bundle non recursive (step 1.2)
			resolvedMessage = interpolateBundleMessage(
					userBundleResolvedMessage,
					defaultResourceBundle,
					locale,
					false
			);
			evaluatedDefaultBundleOnce = true;
		} while ( true );

		return resolvedMessage;
	}

	private String replaceEscapedLiterals(String resolvedMessage) {
		if ( resolvedMessage.indexOf( '\\' ) > -1 ) {
			resolvedMessage = LEFT_BRACE.matcher( resolvedMessage ).replaceAll( "{" );
			resolvedMessage = RIGHT_BRACE.matcher( resolvedMessage ).replaceAll( "}" );
			resolvedMessage = SLASH.matcher( resolvedMessage ).replaceAll( Matcher.quoteReplacement( "\\" ) );
			resolvedMessage = DOLLAR.matcher( resolvedMessage ).replaceAll( Matcher.quoteReplacement( "$" ) );
		}
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

			String resolvedExpression = interpolate( context, locale, term );
			tokenIterator.replaceCurrentInterpolationTerm( resolvedExpression );
		}
		return tokenIterator.getInterpolatedMessage();
	}

	protected abstract String interpolate(Context context, Locale locale, String term);

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
		catch (MissingResourceException e) {
			// return parameter itself
			parameterValue = parameterName;
		}
		return parameterValue;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}
}
