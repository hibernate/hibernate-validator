/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
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
import org.hibernate.validator.internal.engine.valuehandling.OptionalValueUnwrapper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.xml.ValidationBootstrapParameters;
import org.hibernate.validator.internal.xml.ValidationXmlParser;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ConfigurationImpl implements HibernateValidatorConfiguration, ConfigurationState {

	private static final String JFX_UNWRAPPER_CLASS = "org.hibernate.validator.internal.engine.valuehandling.JavaFXPropertyValueUnwrapper";

	static {
		Version.touch();
	}

	private static final Log log = LoggerFactory.make();

	private final ResourceBundleLocator defaultResourceBundleLocator;
	private final MessageInterpolator defaultMessageInterpolator;
	private final TraversableResolver defaultTraversableResolver;
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
	private final ParameterNameProvider defaultParameterNameProvider;
	private final ConstraintDefinitionContributor defaultConstraintDefinitionContributor;

	private ValidationProviderResolver providerResolver;
	private final ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;
	private final Set<InputStream> configurationStreams = CollectionHelper.newHashSet();
	private BootstrapConfiguration bootstrapConfiguration;

	// HV-specific options
	private final Set<DefaultConstraintMapping> programmaticMappings = CollectionHelper.newHashSet();
	private boolean failFast;
	private final Set<ConstraintDefinitionContributor> constraintDefinitionContributors = newHashSet();
	private final List<ValidatedValueUnwrapper<?>> validatedValueHandlers = newArrayList();
	private ClassLoader externalClassLoader;
	private TimeProvider timeProvider;

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
		if ( isJavaFxInClasspath() ) {
			validatedValueHandlers.add( createJavaFXUnwrapperClass( typeResolutionHelper ) );
		}
		if ( Version.getJavaRelease() >= 8 ) {
			validatedValueHandlers.add( new OptionalValueUnwrapper( typeResolutionHelper ) );
		}
		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator(
				ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES
		);
		this.defaultTraversableResolver = new DefaultTraversableResolver();
		this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		this.defaultParameterNameProvider = new DefaultParameterNameProvider();
		this.defaultMessageInterpolator = new ResourceBundleMessageInterpolator( defaultResourceBundleLocator );
		this.defaultConstraintDefinitionContributor = new ServiceLoaderBasedConstraintDefinitionContributor(
				typeResolutionHelper
		);
		this.addConstraintDefinitionContributor( defaultConstraintDefinitionContributor );
	}

	private ValidatedValueUnwrapper<?> createJavaFXUnwrapperClass(TypeResolutionHelper typeResolutionHelper) {
		try {
			Class<?> jfxUnwrapperClass = run( LoadClass.action( JFX_UNWRAPPER_CLASS, getClass().getClassLoader() ) );
			return (ValidatedValueUnwrapper<?>) ( jfxUnwrapperClass.getConstructor( TypeResolutionHelper.class ).newInstance( typeResolutionHelper ) );
		}
		catch (Exception e) {
			throw log.validatedValueUnwrapperCannotBeCreated( JFX_UNWRAPPER_CLASS, e );
		}
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
	public final ConstraintMapping createConstraintMapping() {
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
	public HibernateValidatorConfiguration addValidatedValueHandler(ValidatedValueUnwrapper<?> handler) {
		Contracts.assertNotNull( handler, MESSAGES.parameterMustNotBeNull( "handler" ) );
		validatedValueHandlers.add( handler );

		return this;
	}

	@Override
	public ConstraintDefinitionContributor getDefaultConstraintDefinitionContributor() {
		return defaultConstraintDefinitionContributor;
	}

	public Set<ConstraintDefinitionContributor> getConstraintDefinitionContributors() {
		return constraintDefinitionContributors;
	}

	@Override
	public HibernateValidatorConfiguration addConstraintDefinitionContributor(ConstraintDefinitionContributor contributor) {
		Contracts.assertNotNull( contributor, MESSAGES.parameterMustNotBeNull( "contributor" ) );
		constraintDefinitionContributors.add( contributor );

		return this;
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
				catch ( IOException io ) {
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
		return validationBootstrapParameters.getMessageInterpolator();
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

	public List<ValidatedValueUnwrapper<?>> getValidatedValueHandlers() {
		return validatedValueHandlers;
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

			// make sure we use the defaults in case they haven't been provided yet
			if ( validationBootstrapParameters.getMessageInterpolator() == null ) {
				validationBootstrapParameters.setMessageInterpolator(
						getDefaultMessageInterpolatorConfiguredWithClassLoader()
				);
			}
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
			else {
				validationBootstrapParameters.setMessageInterpolator(
						getDefaultMessageInterpolatorConfiguredWithClassLoader()
				);
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

	private boolean isJavaFxInClasspath() {
		return isClassPresent( "javafx.application.Application", false );
	}

	private boolean isClassPresent(String className, boolean fallbackOnTCCL) {
		try {
			run( LoadClass.action( className, getClass().getClassLoader(), fallbackOnTCCL ) );
			return true;
		}
		catch ( ValidationException e ) {
			return false;
		}
	}

	/**
	 * Returns the default message interpolator, configured with the given user class loader, if present.
	 */
	private MessageInterpolator getDefaultMessageInterpolatorConfiguredWithClassLoader() {
		return externalClassLoader != null ?
				new ResourceBundleMessageInterpolator(
						new PlatformResourceBundleLocator(
								ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES,
								externalClassLoader
						),
						new PlatformResourceBundleLocator(
								ResourceBundleMessageInterpolator.CONTRIBUTOR_VALIDATION_MESSAGES,
								externalClassLoader,
								true
						)
				) :
				defaultMessageInterpolator;
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
