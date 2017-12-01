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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.BootstrapConfiguration;
import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetInstancesFromServiceLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.xml.ValidationBootstrapParameters;
import org.hibernate.validator.internal.xml.ValidationXmlParser;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
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
 */
public class ConfigurationImpl implements HibernateValidatorConfiguration, ConfigurationState {

	static {
		Version.touch();
	}

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ResourceBundleLocator defaultResourceBundleLocator;

	/**
	 * Built lazily so RBMI and its dependency on EL is only initialized if actually needed
	 */
	private MessageInterpolator defaultMessageInterpolator;
	private MessageInterpolator messageInterpolator;

	private final TraversableResolver defaultTraversableResolver;
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
	private final ParameterNameProvider defaultParameterNameProvider;
	private final ClockProvider defaultClockProvider;

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

	public ConfigurationImpl(BootstrapState state) {
		this();
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = state.getDefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
	}

	public ConfigurationImpl(ValidationProvider<?> provider) {
		this();
		if ( provider == null ) {
			throw LOG.getInconsistentConfigurationException();
		}
		this.providerResolver = null;
		validationBootstrapParameters.setProvider( provider );
	}

	private ConfigurationImpl() {
		this.validationBootstrapParameters = new ValidationBootstrapParameters();

		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator(
				ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES
		);
		this.defaultTraversableResolver = TraversableResolvers.getDefault();
		this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		this.defaultParameterNameProvider = new DefaultParameterNameProvider();
		this.defaultClockProvider = DefaultClockProvider.INSTANCE;
	}

	@Override
	public final HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	@Override
	public final ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		if ( LOG.isDebugEnabled() ) {
			if ( interpolator != null ) {
				LOG.debug( "Setting custom MessageInterpolator of type " + interpolator.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setMessageInterpolator( interpolator );
		return this;
	}

	@Override
	public final ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		if ( LOG.isDebugEnabled() ) {
			if ( resolver != null ) {
				LOG.debug( "Setting custom TraversableResolver of type " + resolver.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setTraversableResolver( resolver );
		return this;
	}

	@Override
	public final ConfigurationImpl enableTraversableResolverResultCache(boolean enabled) {
		this.traversableResolverResultCacheEnabled = enabled;
		return this;
	}

	public final boolean isTraversableResolverResultCacheEnabled() {
		return traversableResolverResultCacheEnabled;
	}

	@Override
	public final ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		if ( LOG.isDebugEnabled() ) {
			if ( constraintValidatorFactory != null ) {
				LOG.debug(
						"Setting custom ConstraintValidatorFactory of type " + constraintValidatorFactory.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setConstraintValidatorFactory( constraintValidatorFactory );
		return this;
	}

	@Override
	public HibernateValidatorConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		if ( LOG.isDebugEnabled() ) {
			if ( parameterNameProvider != null ) {
				LOG.debug(
						"Setting custom ParameterNameProvider of type " + parameterNameProvider.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setParameterNameProvider( parameterNameProvider );
		return this;
	}

	@Override
	public HibernateValidatorConfiguration clockProvider(ClockProvider clockProvider) {
		if ( LOG.isDebugEnabled() ) {
			if ( clockProvider != null ) {
				LOG.debug( "Setting custom ClockProvider of type " + clockProvider.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setClockProvider( clockProvider );
		return this;
	}

	@Override
	public HibernateValidatorConfiguration addValueExtractor(ValueExtractor<?> extractor) {
		Contracts.assertNotNull( extractor, MESSAGES.parameterMustNotBeNull( "extractor" ) );

		ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( extractor );
		ValueExtractorDescriptor previous = valueExtractorDescriptors.put( descriptor.getKey(), descriptor );

		if ( previous != null ) {
			throw LOG.getValueExtractorForTypeAndTypeUseAlreadyPresentException( extractor, previous.getValueExtractor() );
		}

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Adding value extractor " + extractor );
		}

		return this;
	}

	@Override
	public final HibernateValidatorConfiguration addMapping(InputStream stream) {
		Contracts.assertNotNull( stream, MESSAGES.inputStreamCannotBeNull() );

		validationBootstrapParameters.addMapping( stream.markSupported() ? stream : new BufferedInputStream( stream ) );
		return this;
	}

	@Override
	public final HibernateValidatorConfiguration failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	@Override
	public HibernateValidatorConfiguration allowOverridingMethodAlterParameterConstraint(boolean allow) {
		this.methodValidationConfigurationBuilder.allowOverridingMethodAlterParameterConstraint( allow );
		return this;
	}

	public boolean isAllowOverridingMethodAlterParameterConstraint() {
		return this.methodValidationConfigurationBuilder.isAllowOverridingMethodAlterParameterConstraint();
	}

	@Override
	public HibernateValidatorConfiguration allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		this.methodValidationConfigurationBuilder.allowMultipleCascadedValidationOnReturnValues( allow );
		return this;
	}

	public boolean isAllowMultipleCascadedValidationOnReturnValues() {
		return this.methodValidationConfigurationBuilder.isAllowMultipleCascadedValidationOnReturnValues();
	}

	@Override
	public HibernateValidatorConfiguration allowParallelMethodsDefineParameterConstraints(boolean allow) {
		this.methodValidationConfigurationBuilder.allowParallelMethodsDefineParameterConstraints( allow );
		return this;
	}

	@Override
	public HibernateValidatorConfiguration scriptEvaluatorFactory(ScriptEvaluatorFactory scriptEvaluatorFactory) {
		Contracts.assertNotNull( scriptEvaluatorFactory, MESSAGES.parameterMustNotBeNull( "scriptEvaluatorFactory" ) );

		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		return this;
	}

	@Override
	public HibernateValidatorConfiguration temporalValidationTolerance(Duration temporalValidationTolerance) {
		Contracts.assertNotNull( temporalValidationTolerance, MESSAGES.parameterMustNotBeNull( "temporalValidationTolerance" ) );

		this.temporalValidationTolerance = temporalValidationTolerance.abs();
		return this;
	}

	@Override
	public HibernateValidatorConfiguration constraintValidatorPayload(Object constraintValidatorPayload) {
		Contracts.assertNotNull( constraintValidatorPayload, MESSAGES.parameterMustNotBeNull( "constraintValidatorPayload" ) );

		this.constraintValidatorPayload = constraintValidatorPayload;
		return this;
	}

	public boolean isAllowParallelMethodsDefineParameterConstraints() {
		return this.methodValidationConfigurationBuilder.isAllowParallelMethodsDefineParameterConstraints();
	}

	public MethodValidationConfiguration getMethodValidationConfiguration() {
		return this.methodValidationConfigurationBuilder.build();
	}

	@Override
	public final DefaultConstraintMapping createConstraintMapping() {
		return new DefaultConstraintMapping();
	}

	@Override
	public final HibernateValidatorConfiguration addMapping(ConstraintMapping mapping) {
		Contracts.assertNotNull( mapping, MESSAGES.parameterMustNotBeNull( "mapping" ) );

		this.programmaticMappings.add( (DefaultConstraintMapping) mapping );
		return this;
	}

	@Override
	public final HibernateValidatorConfiguration addProperty(String name, String value) {
		if ( value != null ) {
			validationBootstrapParameters.addConfigProperty( name, value );
		}
		return this;
	}

	@Override
	public HibernateValidatorConfiguration externalClassLoader(ClassLoader externalClassLoader) {
		Contracts.assertNotNull( externalClassLoader, MESSAGES.parameterMustNotBeNull( "externalClassLoader" ) );
		this.externalClassLoader = externalClassLoader;

		return this;
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
		if ( messageInterpolator == null ) {
			// apply explicitly given MI, otherwise use default one
			MessageInterpolator interpolator = validationBootstrapParameters.getMessageInterpolator();
			if ( interpolator != null ) {
				messageInterpolator = interpolator;
			}
			else {
				messageInterpolator = getDefaultMessageInterpolatorConfiguredWithClassLoader();
			}
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

	public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
		return scriptEvaluatorFactory;
	}

	public Duration getTemporalValidationTolerance() {
		return temporalValidationTolerance;
	}

	public Object getConstraintValidatorPayload() {
		return constraintValidatorPayload;
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
			defaultMessageInterpolator = new ResourceBundleMessageInterpolator( defaultResourceBundleLocator );
		}

		return defaultMessageInterpolator;
	}

	@Override
	public final TraversableResolver getDefaultTraversableResolver() {
		return defaultTraversableResolver;
	}

	@Override
	public final ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	@Override
	public final ResourceBundleLocator getDefaultResourceBundleLocator() {
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
				validationBootstrapParameters.setTraversableResolver( defaultTraversableResolver );
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
				validationBootstrapParameters.setTraversableResolver( defaultTraversableResolver );
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
					externalClassLoader
			);
			PlatformResourceBundleLocator contributorResourceBundleLocator = new PlatformResourceBundleLocator(
					ResourceBundleMessageInterpolator.CONTRIBUTOR_VALIDATION_MESSAGES,
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
						contributorResourceBundleLocator
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
