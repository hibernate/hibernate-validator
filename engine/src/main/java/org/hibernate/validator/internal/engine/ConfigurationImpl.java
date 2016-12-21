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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.xml.ValidationBootstrapParameters;
import org.hibernate.validator.internal.xml.ValidationXmlParser;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.cascading.ValueExtractor;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.time.TimeProvider;

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

	private static final Log log = LoggerFactory.make();

	private final ResourceBundleLocator defaultResourceBundleLocator;

	/**
	 * Built lazily so RBMI and its dependency on EL is only initialized if actually needed
	 */
	private MessageInterpolator defaultMessageInterpolator;
	private MessageInterpolator messageInterpolator;

	private final TraversableResolver defaultTraversableResolver;
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
	private final ParameterNameProvider defaultParameterNameProvider;
	private final ConstraintMappingContributor serviceLoaderBasedConstraintMappingContributor;

	private ValidationProviderResolver providerResolver;
	private final ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;
	private final Set<InputStream> configurationStreams = newHashSet();
	private BootstrapConfiguration bootstrapConfiguration;

	// HV-specific options
	private final Set<DefaultConstraintMapping> programmaticMappings = newHashSet();
	private boolean failFast;
	private final List<ValueExtractor<?>> cascadedValueExtractors = new ArrayList<>();
	private ClassLoader externalClassLoader;
	private TimeProvider timeProvider;
	private final MethodValidationConfiguration methodValidationConfiguration = new MethodValidationConfiguration();

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
			throw log.getInconsistentConfigurationException();
		}
		this.providerResolver = null;
		validationBootstrapParameters.setProvider( provider );
	}

	private ConfigurationImpl() {
		this.validationBootstrapParameters = new ValidationBootstrapParameters();
		TypeResolutionHelper typeResolutionHelper = new TypeResolutionHelper();

		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator(
				ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES
		);
		this.defaultTraversableResolver = new DefaultTraversableResolver();
		this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		this.defaultParameterNameProvider = new DefaultParameterNameProvider();
		this.serviceLoaderBasedConstraintMappingContributor = new ServiceLoaderBasedConstraintMappingContributor(
				typeResolutionHelper
		);
	}

	@Override
	public final HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	@Override
	public final ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		if ( log.isDebugEnabled() ) {
			if ( interpolator != null ) {
				log.debug( "Setting custom MessageInterpolator of type " + interpolator.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setMessageInterpolator( interpolator );
		return this;
	}

	@Override
	public final ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		if ( log.isDebugEnabled() ) {
			if ( resolver != null ) {
				log.debug( "Setting custom TraversableResolver of type " + resolver.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setTraversableResolver( resolver );
		return this;
	}

	@Override
	public final ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		if ( log.isDebugEnabled() ) {
			if ( constraintValidatorFactory != null ) {
				log.debug(
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
		if ( log.isDebugEnabled() ) {
			if ( parameterNameProvider != null ) {
				log.debug(
						"Setting custom ParameterNameProvider of type " + parameterNameProvider.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setParameterNameProvider( parameterNameProvider );
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
		this.methodValidationConfiguration.allowOverridingMethodAlterParameterConstraint( allow );
		return this;
	}

	public boolean isAllowOverridingMethodAlterParameterConstraint() {
		return this.methodValidationConfiguration.isAllowOverridingMethodAlterParameterConstraint();
	}

	@Override
	public HibernateValidatorConfiguration allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		this.methodValidationConfiguration.allowMultipleCascadedValidationOnReturnValues( allow );
		return this;
	}

	public boolean isAllowMultipleCascadedValidationOnReturnValues() {
		return this.methodValidationConfiguration.isAllowMultipleCascadedValidationOnReturnValues();
	}

	@Override
	public HibernateValidatorConfiguration allowParallelMethodsDefineParameterConstraints(boolean allow) {
		this.methodValidationConfiguration.allowParallelMethodsDefineParameterConstraints( allow );
		return this;
	}

	public boolean isAllowParallelMethodsDefineParameterConstraints() {
		return this.methodValidationConfiguration.isAllowParallelMethodsDefineParameterConstraints();
	}

	public MethodValidationConfiguration getMethodValidationConfiguration() {
		return this.methodValidationConfiguration;
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
	public HibernateValidatorConfiguration addCascadedValueExtractor(ValueExtractor<?> extractor) {
		Contracts.assertNotNull( extractor, MESSAGES.parameterMustNotBeNull( "extractor" ) );
		cascadedValueExtractors.add( extractor );

		return this;
	}


	public final ConstraintMappingContributor getServiceLoaderBasedConstraintMappingContributor() {
		return serviceLoaderBasedConstraintMappingContributor;
	}

	@Override
	public HibernateValidatorConfiguration externalClassLoader(ClassLoader externalClassLoader) {
		Contracts.assertNotNull( externalClassLoader, MESSAGES.parameterMustNotBeNull( "externalClassLoader" ) );
		this.externalClassLoader = externalClassLoader;

		return this;
	}

	@Override
	public HibernateValidatorConfiguration timeProvider(TimeProvider timeProvider) {
		Contracts.assertNotNull( timeProvider, MESSAGES.parameterMustNotBeNull( "timeProvider" ) );
		this.timeProvider = timeProvider;

		return this;
	}

	@Override
	public final ValidatorFactory buildValidatorFactory() {
		parseValidationXml();
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
						throw log.getUnableToFindProviderException( providerClass );
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
					log.unableToCloseInputStream();
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

	public List<ValueExtractor<?>> getCascadedValueExtractors() {
		return cascadedValueExtractors;
	}

	public TimeProvider getTimeProvider() {
		return timeProvider;
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
			log.ignoringXmlConfiguration();

			if ( validationBootstrapParameters.getTraversableResolver() == null ) {
				validationBootstrapParameters.setTraversableResolver( defaultTraversableResolver );
			}
			if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
				validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
			}
			if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
				validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
			}
		}
		else {
			ValidationBootstrapParameters xmlParameters = new ValidationBootstrapParameters(
					getBootstrapConfiguration(), externalClassLoader
			);
			applyXmlSettings( xmlParameters );
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
