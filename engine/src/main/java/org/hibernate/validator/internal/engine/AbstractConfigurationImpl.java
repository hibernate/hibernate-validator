/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidationProviderResolver;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetInstancesFromServiceLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.internal.xml.config.ValidationXmlParser;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 */
public abstract class AbstractConfigurationImpl<T extends BaseHibernateValidatorConfiguration<T>>
		implements BaseHibernateValidatorConfiguration<T>, ConfigurationState {

	static {
		Version.touch();
	}

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );


	/**
	 * Built lazily so RBMI and its dependency on EL is only initialized if actually needed
	 */
	@Lazy
	private ResourceBundleLocator defaultResourceBundleLocator;
	@Lazy
	private MessageInterpolator defaultMessageInterpolator;
	@Lazy
	private MessageInterpolator messageInterpolator;

	/**
	 * Created lazily to avoid fishing in the classpath if one has been defined.
	 */
	@Lazy
	private TraversableResolver defaultTraversableResolver;

	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
	private final ParameterNameProvider defaultParameterNameProvider;
	private final ClockProvider defaultClockProvider;
	private final PropertyNodeNameProvider defaultPropertyNodeNameProvider;

	private ValidationProviderResolver providerResolver;
	private final ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;
	private final Set<InputStream> configurationStreams = newHashSet();
	private BootstrapConfiguration bootstrapConfiguration;

	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractorDescriptors = new HashMap<>();

	// HV-specific options
	private final Set<DefaultConstraintMapping> programmaticMappings = newHashSet();
	private boolean failFast;
	private ClassLoader externalClassLoader;
	private final MethodValidationConfiguration.Builder methodValidationConfigurationBuilder = new MethodValidationConfiguration.Builder();
	private boolean traversableResolverResultCacheEnabled = true;
	private ScriptEvaluatorFactory scriptEvaluatorFactory;
	private Duration temporalValidationTolerance;
	private Object constraintValidatorPayload;
	private GetterPropertySelectionStrategy getterPropertySelectionStrategy;
	private Set<Locale> locales = Collections.emptySet();
	private Locale defaultLocale = Locale.getDefault();
	private LocaleResolver localeResolver;
	private BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;
	private ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel;
	private ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel;

	protected AbstractConfigurationImpl(BootstrapState state) {
		this();
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = state.getDefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
	}

	protected AbstractConfigurationImpl(ValidationProvider<?> provider) {
		this();
		if ( provider == null ) {
			throw LOG.getInconsistentConfigurationException();
		}
		this.providerResolver = null;
		validationBootstrapParameters.setProvider( provider );
	}

	private AbstractConfigurationImpl() {
		this.validationBootstrapParameters = new ValidationBootstrapParameters();

		this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		this.defaultParameterNameProvider = new DefaultParameterNameProvider();
		this.defaultClockProvider = DefaultClockProvider.INSTANCE;
		this.defaultPropertyNodeNameProvider = new DefaultPropertyNodeNameProvider();
	}

	@Override
	public final T ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return thisAsT();
	}

	@Override
	public final T messageInterpolator(MessageInterpolator interpolator) {
		if ( LOG.isDebugEnabled() ) {
			if ( interpolator != null ) {
				LOG.debug( "Setting custom MessageInterpolator of type " + interpolator.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setMessageInterpolator( interpolator );
		return thisAsT();
	}

	@Override
	public final T traversableResolver(TraversableResolver resolver) {
		if ( LOG.isDebugEnabled() ) {
			if ( resolver != null ) {
				LOG.debug( "Setting custom TraversableResolver of type " + resolver.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setTraversableResolver( resolver );
		return thisAsT();
	}

	@Override
	public final T enableTraversableResolverResultCache(boolean enabled) {
		this.traversableResolverResultCacheEnabled = enabled;
		return thisAsT();
	}

	public final boolean isTraversableResolverResultCacheEnabled() {
		return traversableResolverResultCacheEnabled;
	}

	@Override
	public final T constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		if ( LOG.isDebugEnabled() ) {
			if ( constraintValidatorFactory != null ) {
				LOG.debug(
						"Setting custom ConstraintValidatorFactory of type " + constraintValidatorFactory.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setConstraintValidatorFactory( constraintValidatorFactory );
		return thisAsT();
	}

	@Override
	public T parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		if ( LOG.isDebugEnabled() ) {
			if ( parameterNameProvider != null ) {
				LOG.debug(
						"Setting custom ParameterNameProvider of type " + parameterNameProvider.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setParameterNameProvider( parameterNameProvider );
		return thisAsT();
	}

	@Override
	public T clockProvider(ClockProvider clockProvider) {
		if ( LOG.isDebugEnabled() ) {
			if ( clockProvider != null ) {
				LOG.debug( "Setting custom ClockProvider of type " + clockProvider.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setClockProvider( clockProvider );
		return thisAsT();
	}

	@Override
	public T propertyNodeNameProvider(PropertyNodeNameProvider propertyNodeNameProvider) {
		if ( LOG.isDebugEnabled() ) {
			if ( propertyNodeNameProvider != null ) {
				LOG.debug( "Setting custom PropertyNodeNameProvider of type " + propertyNodeNameProvider.getClass()
						.getName() );
			}
		}
		this.validationBootstrapParameters.setPropertyNodeNameProvider( propertyNodeNameProvider );

		return thisAsT();
	}

	@Override
	public T localeResolver(LocaleResolver localeResolver) {
		if ( LOG.isDebugEnabled() ) {
			if ( localeResolver != null ) {
				LOG.debug( "Setting custom LocaleResolver of type " + localeResolver.getClass()
						.getName() );
			}
		}
		this.localeResolver = localeResolver;

		return thisAsT();
	}

	@Override
	public T addValueExtractor(ValueExtractor<?> extractor) {
		Contracts.assertNotNull( extractor, MESSAGES.parameterMustNotBeNull( "extractor" ) );

		ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( extractor );
		ValueExtractorDescriptor previous = valueExtractorDescriptors.put( descriptor.getKey(), descriptor );

		if ( previous != null ) {
			throw LOG.getValueExtractorForTypeAndTypeUseAlreadyPresentException( extractor, previous.getValueExtractor() );
		}

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Adding value extractor " + extractor );
		}

		return thisAsT();
	}

	@Override
	public final T addMapping(InputStream stream) {
		Contracts.assertNotNull( stream, MESSAGES.inputStreamCannotBeNull() );

		validationBootstrapParameters.addMapping( stream.markSupported() ? stream : new BufferedInputStream( stream ) );
		return thisAsT();
	}

	@Override
	public final T failFast(boolean failFast) {
		this.failFast = failFast;
		return thisAsT();
	}

	@Override
	public T allowOverridingMethodAlterParameterConstraint(boolean allow) {
		this.methodValidationConfigurationBuilder.allowOverridingMethodAlterParameterConstraint( allow );
		return thisAsT();
	}

	public boolean isAllowOverridingMethodAlterParameterConstraint() {
		return this.methodValidationConfigurationBuilder.isAllowOverridingMethodAlterParameterConstraint();
	}

	@Override
	public T allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		this.methodValidationConfigurationBuilder.allowMultipleCascadedValidationOnReturnValues( allow );
		return thisAsT();
	}

	public boolean isAllowMultipleCascadedValidationOnReturnValues() {
		return this.methodValidationConfigurationBuilder.isAllowMultipleCascadedValidationOnReturnValues();
	}

	@Override
	public T allowParallelMethodsDefineParameterConstraints(boolean allow) {
		this.methodValidationConfigurationBuilder.allowParallelMethodsDefineParameterConstraints( allow );
		return thisAsT();
	}

	@Override
	public T scriptEvaluatorFactory(ScriptEvaluatorFactory scriptEvaluatorFactory) {
		Contracts.assertNotNull( scriptEvaluatorFactory, MESSAGES.parameterMustNotBeNull( "scriptEvaluatorFactory" ) );

		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		return thisAsT();
	}

	@Override
	public T temporalValidationTolerance(Duration temporalValidationTolerance) {
		Contracts.assertNotNull( temporalValidationTolerance, MESSAGES.parameterMustNotBeNull( "temporalValidationTolerance" ) );

		this.temporalValidationTolerance = temporalValidationTolerance.abs();
		return thisAsT();
	}

	@Override
	public T constraintValidatorPayload(Object constraintValidatorPayload) {
		Contracts.assertNotNull( constraintValidatorPayload, MESSAGES.parameterMustNotBeNull( "constraintValidatorPayload" ) );

		this.constraintValidatorPayload = constraintValidatorPayload;
		return thisAsT();
	}

	@Override
	public T getterPropertySelectionStrategy(GetterPropertySelectionStrategy getterPropertySelectionStrategy) {
		Contracts.assertNotNull( getterPropertySelectionStrategy, MESSAGES.parameterMustNotBeNull( "getterPropertySelectionStrategy" ) );

		this.getterPropertySelectionStrategy = getterPropertySelectionStrategy;
		return thisAsT();
	}

	@Override
	public T locales(Set<Locale> locales) {
		Contracts.assertNotNull( defaultLocale, MESSAGES.parameterMustNotBeNull( "locales" ) );

		this.locales = locales;
		return thisAsT();
	}

	@Override
	public T defaultLocale(Locale defaultLocale) {
		Contracts.assertNotNull( defaultLocale, MESSAGES.parameterMustNotBeNull( "defaultLocale" ) );

		this.defaultLocale = defaultLocale;
		return thisAsT();
	}

	public boolean isAllowParallelMethodsDefineParameterConstraints() {
		return this.methodValidationConfigurationBuilder.isAllowParallelMethodsDefineParameterConstraints();
	}

	public MethodValidationConfiguration getMethodValidationConfiguration() {
		return this.methodValidationConfigurationBuilder.build();
	}

	@Override
	public final DefaultConstraintMapping createConstraintMapping() {
		GetterPropertySelectionStrategy getterPropertySelectionStrategyToUse = null;
		if ( getterPropertySelectionStrategy == null ) {
			getterPropertySelectionStrategyToUse = new DefaultGetterPropertySelectionStrategy();
		}
		else {
			getterPropertySelectionStrategyToUse = getterPropertySelectionStrategy;
		}

		return new DefaultConstraintMapping( new JavaBeanHelper( getterPropertySelectionStrategyToUse, defaultPropertyNodeNameProvider ) );
	}

	@Override
	public final T addMapping(ConstraintMapping mapping) {
		Contracts.assertNotNull( mapping, MESSAGES.parameterMustNotBeNull( "mapping" ) );

		this.programmaticMappings.add( (DefaultConstraintMapping) mapping );
		return thisAsT();
	}

	@Override
	public final T addProperty(String name, String value) {
		if ( value != null ) {
			validationBootstrapParameters.addConfigProperty( name, value );
		}
		return thisAsT();
	}

	@Override
	public T externalClassLoader(ClassLoader externalClassLoader) {
		Contracts.assertNotNull( externalClassLoader, MESSAGES.parameterMustNotBeNull( "externalClassLoader" ) );
		this.externalClassLoader = externalClassLoader;

		// we need to reset the messageInterpolator field as it might vary depending on the class loader
		this.messageInterpolator = null;

		return thisAsT();
	}

	@Override
	public final ValidatorFactory buildValidatorFactory() {
		loadValueExtractorsFromServiceLoader();
		parseValidationXml();

		for ( ValueExtractorDescriptor valueExtractorDescriptor : valueExtractorDescriptors.values() ) {
			validationBootstrapParameters.addValueExtractorDescriptor( valueExtractorDescriptor );
		}

		ValidatorFactory factory = null;
		try {
			if ( isSpecificProvider() ) {
				factory = validationBootstrapParameters.getProvider().buildValidatorFactory( this );
			}
			else {
				final Class<? extends ValidationProvider<?>> providerClass = validationBootstrapParameters.getProviderClass();
				if ( providerClass != null ) {
					for ( ValidationProvider<?> provider : providerResolver.getValidationProviders() ) {
						if ( providerClass.isAssignableFrom( provider.getClass() ) ) {
							factory = provider.buildValidatorFactory( this );
							break;
						}
					}
					if ( factory == null ) {
						throw LOG.getUnableToFindProviderException( providerClass );
					}
				}
				else {
					List<ValidationProvider<?>> providers = providerResolver.getValidationProviders();
					assert providers.size() != 0; // I run therefore I am
					factory = providers.get( 0 ).buildValidatorFactory( this );
				}
			}
		}
		finally {
			// close all input streams opened by this configuration
			for ( InputStream in : configurationStreams ) {
				try {
					in.close();
				}
				catch (IOException io) {
					LOG.unableToCloseInputStream();
				}
			}
		}

		return factory;
	}

	@Override
	public final boolean isIgnoreXmlConfiguration() {
		return ignoreXmlConfiguration;
	}

	@Override
	public final MessageInterpolator getMessageInterpolator() {
		// apply explicitly given MI, otherwise use default one
		MessageInterpolator selectedInterpolator = validationBootstrapParameters.getMessageInterpolator();
		if ( selectedInterpolator != null ) {
			return selectedInterpolator;
		}
		if ( messageInterpolator == null ) {
			messageInterpolator = getDefaultMessageInterpolatorConfiguredWithClassLoader();
		}

		return messageInterpolator;
	}

	@Override
	public final Set<InputStream> getMappingStreams() {
		return validationBootstrapParameters.getMappings();
	}

	public final boolean getFailFast() {
		return failFast;
	}

	@Override
	public final ConstraintValidatorFactory getConstraintValidatorFactory() {
		return validationBootstrapParameters.getConstraintValidatorFactory();
	}

	@Override
	public final TraversableResolver getTraversableResolver() {
		return validationBootstrapParameters.getTraversableResolver();
	}

	@Override
	public BootstrapConfiguration getBootstrapConfiguration() {
		if ( bootstrapConfiguration == null ) {
			bootstrapConfiguration = new ValidationXmlParser( externalClassLoader ).parseValidationXml();
		}
		return bootstrapConfiguration;
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return validationBootstrapParameters.getParameterNameProvider();
	}

	@Override
	public ClockProvider getClockProvider() {
		return validationBootstrapParameters.getClockProvider();
	}

	public PropertyNodeNameProvider getPropertyNodeNameProvider() {
		return validationBootstrapParameters.getPropertyNodeNameProvider();
	}

	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
		return scriptEvaluatorFactory;
	}

	public Duration getTemporalValidationTolerance() {
		return temporalValidationTolerance;
	}

	public Object getConstraintValidatorPayload() {
		return constraintValidatorPayload;
	}

	public GetterPropertySelectionStrategy getGetterPropertySelectionStrategy() {
		return getterPropertySelectionStrategy;
	}

	@Override
	public Set<ValueExtractor<?>> getValueExtractors() {
		return validationBootstrapParameters.getValueExtractorDescriptors()
				.values()
				.stream()
				.map( ValueExtractorDescriptor::getValueExtractor )
				.collect( Collectors.toSet() );
	}

	@Override
	public final Map<String, String> getProperties() {
		return validationBootstrapParameters.getConfigProperties();
	}

	public ClassLoader getExternalClassLoader() {
		return externalClassLoader;
	}

	@Override
	public final MessageInterpolator getDefaultMessageInterpolator() {
		if ( defaultMessageInterpolator == null ) {
			defaultMessageInterpolator = new ResourceBundleMessageInterpolator( getDefaultResourceBundleLocator(), getAllSupportedLocales(),
					defaultLocale, ValidatorFactoryConfigurationHelper.determineLocaleResolver( this, this.getProperties(), externalClassLoader ),
					preloadResourceBundles() );
		}

		return defaultMessageInterpolator;
	}

	@Override
	public final TraversableResolver getDefaultTraversableResolver() {
		if ( defaultTraversableResolver == null ) {
			defaultTraversableResolver = TraversableResolvers.getDefault();
		}
		return defaultTraversableResolver;
	}

	@Override
	public final ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	@Override
	public final ResourceBundleLocator getDefaultResourceBundleLocator() {
		if ( defaultResourceBundleLocator == null ) {
			defaultResourceBundleLocator = new PlatformResourceBundleLocator(
					ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES, preloadResourceBundles() ? getAllSupportedLocales() : Collections.emptySet() );
		}

		return defaultResourceBundleLocator;
	}

	@Override
	public ParameterNameProvider getDefaultParameterNameProvider() {
		return defaultParameterNameProvider;
	}

	@Override
	public ClockProvider getDefaultClockProvider() {
		return defaultClockProvider;
	}

	@Override
	public Set<ValueExtractor<?>> getDefaultValueExtractors() {
		return ValueExtractorManager.getDefaultValueExtractors();
	}

	@Override
	public T beanMetaDataClassNormalizer(BeanMetaDataClassNormalizer beanMetaDataClassNormalizer) {
		if ( LOG.isDebugEnabled() ) {
			if ( beanMetaDataClassNormalizer != null ) {
				LOG.debug( "Setting custom BeanMetaDataClassNormalizer of type " + beanMetaDataClassNormalizer.getClass()
						.getName() );
			}
		}
		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
		return thisAsT();
	}

	public BeanMetaDataClassNormalizer getBeanMetaDataClassNormalizer() {
		return beanMetaDataClassNormalizer;
	}

	@Override
	public T constraintExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel) {
		if ( LOG.isDebugEnabled() ) {
			if ( expressionLanguageFeatureLevel != null ) {
				LOG.debug( "Setting ExpressionLanguageFeatureLevel for constraints to " + expressionLanguageFeatureLevel.name() );
			}
		}
		this.constraintExpressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
		return thisAsT();
	}

	public ExpressionLanguageFeatureLevel getConstraintExpressionLanguageFeatureLevel() {
		return constraintExpressionLanguageFeatureLevel;
	}

	@Override
	public T customViolationExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel) {
		if ( LOG.isDebugEnabled() ) {
			if ( expressionLanguageFeatureLevel != null ) {
				LOG.debug( "Setting ExpressionLanguageFeatureLevel for custom violations to " + expressionLanguageFeatureLevel.name() );
			}
		}
		this.customViolationExpressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
		return thisAsT();
	}

	public ExpressionLanguageFeatureLevel getCustomViolationExpressionLanguageFeatureLevel() {
		return customViolationExpressionLanguageFeatureLevel;
	}

	public final Set<DefaultConstraintMapping> getProgrammaticMappings() {
		return programmaticMappings;
	}

	private boolean isSpecificProvider() {
		return validationBootstrapParameters.getProvider() != null;
	}

	/**
	 * Tries to check whether a validation.xml file exists and parses it
	 */
	private void parseValidationXml() {
		if ( ignoreXmlConfiguration ) {
			LOG.ignoringXmlConfiguration();

			if ( validationBootstrapParameters.getTraversableResolver() == null ) {
				validationBootstrapParameters.setTraversableResolver( getDefaultTraversableResolver() );
			}
			if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
				validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
			}
			if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
				validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
			}
			if ( validationBootstrapParameters.getClockProvider() == null ) {
				validationBootstrapParameters.setClockProvider( defaultClockProvider );
			}
			if ( validationBootstrapParameters.getPropertyNodeNameProvider() == null ) {
				validationBootstrapParameters.setPropertyNodeNameProvider( defaultPropertyNodeNameProvider );
			}
		}
		else {
			ValidationBootstrapParameters xmlParameters = new ValidationBootstrapParameters(
					getBootstrapConfiguration(), externalClassLoader
			);
			applyXmlSettings( xmlParameters );
		}
	}

	@SuppressWarnings("rawtypes")
	private void loadValueExtractorsFromServiceLoader() {
		List<ValueExtractor> valueExtractors = run( GetInstancesFromServiceLoader.action(
				externalClassLoader != null ? externalClassLoader : run( GetClassLoader.fromContext() ),
				ValueExtractor.class
		) );

		for ( ValueExtractor<?> valueExtractor : valueExtractors ) {
			validationBootstrapParameters.addValueExtractorDescriptor( new ValueExtractorDescriptor( valueExtractor ) );
		}
	}

	private void applyXmlSettings(ValidationBootstrapParameters xmlParameters) {
		validationBootstrapParameters.setProviderClass( xmlParameters.getProviderClass() );

		if ( validationBootstrapParameters.getMessageInterpolator() == null ) {
			if ( xmlParameters.getMessageInterpolator() != null ) {
				validationBootstrapParameters.setMessageInterpolator( xmlParameters.getMessageInterpolator() );
			}
		}

		if ( validationBootstrapParameters.getTraversableResolver() == null ) {
			if ( xmlParameters.getTraversableResolver() != null ) {
				validationBootstrapParameters.setTraversableResolver( xmlParameters.getTraversableResolver() );
			}
			else {
				validationBootstrapParameters.setTraversableResolver( getDefaultTraversableResolver() );
			}
		}

		if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
			if ( xmlParameters.getConstraintValidatorFactory() != null ) {
				validationBootstrapParameters.setConstraintValidatorFactory(
						xmlParameters.getConstraintValidatorFactory()
				);
			}
			else {
				validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
			}
		}

		if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
			if ( xmlParameters.getParameterNameProvider() != null ) {
				validationBootstrapParameters.setParameterNameProvider( xmlParameters.getParameterNameProvider() );
			}
			else {
				validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
			}
		}

		if ( validationBootstrapParameters.getClockProvider() == null ) {
			if ( xmlParameters.getClockProvider() != null ) {
				validationBootstrapParameters.setClockProvider( xmlParameters.getClockProvider() );
			}
			else {
				validationBootstrapParameters.setClockProvider( defaultClockProvider );
			}
		}

		if ( validationBootstrapParameters.getPropertyNodeNameProvider() == null ) {
			if ( xmlParameters.getPropertyNodeNameProvider() != null ) {
				validationBootstrapParameters.setPropertyNodeNameProvider(
						xmlParameters.getPropertyNodeNameProvider() );
			}
			else {
				validationBootstrapParameters.setPropertyNodeNameProvider( defaultPropertyNodeNameProvider );
			}
		}

		for ( ValueExtractorDescriptor valueExtractorDescriptor : xmlParameters.getValueExtractorDescriptors().values() ) {
			validationBootstrapParameters.addValueExtractorDescriptor( valueExtractorDescriptor );
		}

		validationBootstrapParameters.addAllMappings( xmlParameters.getMappings() );
		configurationStreams.addAll( xmlParameters.getMappings() );

		for ( Map.Entry<String, String> entry : xmlParameters.getConfigProperties().entrySet() ) {
			if ( validationBootstrapParameters.getConfigProperties().get( entry.getKey() ) == null ) {
				validationBootstrapParameters.addConfigProperty( entry.getKey(), entry.getValue() );
			}
		}
	}

	/**
	 * Returns the default message interpolator, configured with the given user class loader, if present.
	 */
	private MessageInterpolator getDefaultMessageInterpolatorConfiguredWithClassLoader() {
		if ( externalClassLoader != null ) {
			PlatformResourceBundleLocator userResourceBundleLocator = new PlatformResourceBundleLocator(
					ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES,
					preloadResourceBundles() ? getAllSupportedLocales() : Collections.emptySet(),
					externalClassLoader
			);
			PlatformResourceBundleLocator contributorResourceBundleLocator = new PlatformResourceBundleLocator(
					ResourceBundleMessageInterpolator.CONTRIBUTOR_VALIDATION_MESSAGES,
					preloadResourceBundles() ? getAllSupportedLocales() : Collections.emptySet(),
					externalClassLoader,
					true
			);

			// Within RBMI, the expression factory implementation is loaded from the TCCL; thus we set the TCCL to the
			// given external class loader for this call
			final ClassLoader originalContextClassLoader = run( GetClassLoader.fromContext() );

			try {
				run( SetContextClassLoader.action( externalClassLoader ) );
				return new ResourceBundleMessageInterpolator(
						userResourceBundleLocator,
						contributorResourceBundleLocator,
						getAllSupportedLocales(),
						defaultLocale,
						ValidatorFactoryConfigurationHelper.determineLocaleResolver( this, this.getProperties(), externalClassLoader ),
						preloadResourceBundles()
				);
			}
			finally {
				run( SetContextClassLoader.action( originalContextClassLoader ) );
			}
		}
		else {
			return getDefaultMessageInterpolator();
		}
	}

	private Set<Locale> getAllSupportedLocales() {
		if ( locales.isEmpty() ) {
			return Collections.singleton( defaultLocale );
		}
		if ( locales.contains( defaultLocale ) ) {
			return locales;
		}

		Set<Locale> allLocales = CollectionHelper.newHashSet( locales.size() + 1 );
		allLocales.addAll( locales );
		allLocales.add( defaultLocale );
		return allLocales;
	}

	protected abstract boolean preloadResourceBundles();

	@SuppressWarnings("unchecked")
	protected T thisAsT() {
		return (T) this;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
