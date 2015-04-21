/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionBuilderImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.internal.engine.time.DefaultTimeProvider;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Factory returning initialized {@code Validator} instances. This is the Hibernate Validator default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ValidatorFactoryImpl implements HibernateValidatorFactory {

	private static final Log log = LoggerFactory.make();

	/**
	 * The default message interpolator for this factory.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The default traversable resolver for this factory.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * The default parameter name provider for this factory.
	 */
	private final ParameterNameProvider parameterNameProvider;

	/**
	 * Provider for the current time when validating {@code @Future} or code @Past}
	 */
	private final TimeProvider timeProvider;

	/**
	 * The default constraint validator factory for this factory.
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	/**
	 * Programmatic constraints passed via the Hibernate Validator specific API. Empty if there are
	 * no programmatic constraints
	 */
	private final Set<DefaultConstraintMapping> constraintMappings;

	/**
	 * Helper for dealing with built-in validators and determining custom constraint annotations.
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * Used for discovering overridden methods. Thread-safe.
	 */
	private final ExecutableHelper executableHelper;

	/**
	 * Hibernate Validator specific flag to abort validation on first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * Metadata provider for XML configuration.
	 */
	private XmlMetaDataProvider xmlMetaDataProvider;

	/**
	 * Prior to the introduction of {@code ParameterNameProvider} all the bean meta data was static and could be
	 * cached for all created {@code Validator}s. {@code ParameterNameProvider} makes parts of the meta data and
	 * Bean Validation element descriptors dynamic, since depending of the used provider different parameter names
	 * could be used. To still have the metadata static we create a {@code BeanMetaDataManager} per parameter name
	 * provider. See also HV-659.
	 */
	private final Map<ParameterNameProvider, BeanMetaDataManager> beanMetaDataManagerMap;

	/**
	 * Contains handlers to be applied to the validated value when validating elements.
	 */
	private final List<ValidatedValueUnwrapper<?>> validatedValueHandlers;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		ClassLoader externalClassLoader = getExternalClassLoader( configurationState );

		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.parameterNameProvider = configurationState.getParameterNameProvider();
		this.timeProvider = getTimeProvider( configurationState, externalClassLoader );
		this.beanMetaDataManagerMap = Collections.synchronizedMap( new IdentityHashMap<ParameterNameProvider, BeanMetaDataManager>() );
		this.constraintHelper = new ConstraintHelper();
		this.typeResolutionHelper = new TypeResolutionHelper();
		this.executableHelper = new ExecutableHelper( typeResolutionHelper );


		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider(
					constraintHelper, parameterNameProvider, configurationState.getMappingStreams(), externalClassLoader
			);
		}

		Map<String, String> properties = configurationState.getProperties();

		boolean tmpFailFast = false;
		List<ValidatedValueUnwrapper<?>> tmpValidatedValueHandlers = newArrayList( 5 );

		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

			// check whether fail fast is programmatically enabled
			tmpFailFast = hibernateSpecificConfig.getFailFast();

			tmpValidatedValueHandlers.addAll( hibernateSpecificConfig.getValidatedValueHandlers() );

			registerCustomConstraintValidators( hibernateSpecificConfig, properties, externalClassLoader, constraintHelper );
		}

		this.constraintMappings = Collections.unmodifiableSet( getConstraintMappings( configurationState, externalClassLoader ) );

		tmpFailFast = checkPropertiesForFailFast(
				properties, tmpFailFast
		);
		this.failFast = tmpFailFast;

		tmpValidatedValueHandlers.addAll( getPropertyConfiguredValidatedValueHandlers( properties, externalClassLoader ) );
		this.validatedValueHandlers = Collections.unmodifiableList( tmpValidatedValueHandlers );

		this.constraintValidatorManager = new ConstraintValidatorManager( configurationState.getConstraintValidatorFactory() );
	}

	private static ClassLoader getExternalClassLoader(ConfigurationState configurationState) {
		return ( configurationState instanceof ConfigurationImpl ) ? ( (ConfigurationImpl) configurationState ).getExternalClassLoader() : null;
	}
	
	private static Set<DefaultConstraintMapping> getConstraintMappings(ConfigurationState configurationState, ClassLoader externalClassLoader) {
		Set<DefaultConstraintMapping> constraintMappings;

		// programmatic config
		if ( configurationState instanceof ConfigurationImpl ) {
			constraintMappings = ( (ConfigurationImpl) configurationState ).getProgrammaticMappings();
		}
		else {
			constraintMappings = newHashSet();
		}

		// XML
		String constraintMappingContributorClassName = configurationState.getProperties()
				.get( HibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTOR );

		if ( constraintMappingContributorClassName != null ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintMappingContributor> contributorType = (Class<? extends ConstraintMappingContributor>) run(
					LoadClass
							.action( constraintMappingContributorClassName, externalClassLoader )
			);

			ConstraintMappingContributor contributor = run(
					NewInstance.action(
							contributorType,
							"constraint mapping contributor class"
					)
			);
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder();
			contributor.createConstraintMappings( builder );

			constraintMappings.addAll( builder.mappings );
		}

		return constraintMappings;
	}

	private static TimeProvider getTimeProvider(ConfigurationState configurationState, ClassLoader externalClassLoader) {
		TimeProvider timeProvider = null;

		// programmatic config
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hvConfig = (ConfigurationImpl) configurationState;
			timeProvider = hvConfig.getTimeProvider();
		}

		// XML config
		if ( timeProvider == null ) {
			String timeProviderClassName = configurationState.getProperties().get( HibernateValidatorConfiguration.TIME_PROVIDER );

			if ( timeProviderClassName != null ) {
				@SuppressWarnings("unchecked")
				Class<? extends TimeProvider> handlerType = (Class<? extends TimeProvider>) run( LoadClass
						.action( timeProviderClassName, externalClassLoader ) );
				timeProvider = run( NewInstance.action( handlerType, "time provider class" ) );
			}
		}

		return timeProvider != null ? timeProvider : DefaultTimeProvider.getInstance();
	}

	@Override
	public Validator getValidator() {
		return createValidator(
				constraintValidatorManager.getDefaultConstraintValidatorFactory(),
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				failFast,
				validatedValueHandlers,
				timeProvider
		);
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorManager.getDefaultConstraintValidatorFactory();
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return parameterNameProvider;
	}

	public boolean isFailFast() {
		return failFast;
	}

	public List<ValidatedValueUnwrapper<?>> getValidatedValueHandlers() {
		return validatedValueHandlers;
	}

	TimeProvider getTimeProvider() {
		return timeProvider;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateValidatorFactory.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateValidatorContext usingContext() {
		return new ValidatorContextImpl( this );
	}

	@Override
	public void close() {
		constraintValidatorManager.clear();
		for ( BeanMetaDataManager beanMetaDataManager : beanMetaDataManagerMap.values() ) {
			beanMetaDataManager.clear();
		}

		// this holds a reference to the provided external class-loader, thus freeing it to be on the safe side
		xmlMetaDataProvider = null;
	}

	Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory,
			MessageInterpolator messageInterpolator,
			TraversableResolver traversableResolver,
			ParameterNameProvider parameterNameProvider,
			boolean failFast,
			List<ValidatedValueUnwrapper<?>> validatedValueHandlers,
			TimeProvider timeProvider) {

		// HV-793 - To fail eagerly in case we have no EL dependencies on the classpath we try to load the expression
		// factory
		if( messageInterpolator instanceof ResourceBundleMessageInterpolator ) {
			try {
				ResourceBundleMessageInterpolator.class.getClassLoader().loadClass( "javax.el.ExpressionFactory" );
			}
			catch ( ClassNotFoundException e ) {
				throw log.getMissingELDependenciesException();
			}
		}

		BeanMetaDataManager beanMetaDataManager;
		if ( !beanMetaDataManagerMap.containsKey( parameterNameProvider ) ) {
			beanMetaDataManager = new BeanMetaDataManager(
					constraintHelper,
					executableHelper,
					parameterNameProvider,
					buildDataProviders( parameterNameProvider )
			);
			beanMetaDataManagerMap.put( parameterNameProvider, beanMetaDataManager );
		}
		else {
			beanMetaDataManager = beanMetaDataManagerMap.get( parameterNameProvider );
		}

		return new ValidatorImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				beanMetaDataManager,
				parameterNameProvider,
				timeProvider,
				typeResolutionHelper,
				validatedValueHandlers,
				constraintValidatorManager,
				failFast
		);
	}

	private List<MetaDataProvider> buildDataProviders(ParameterNameProvider parameterNameProvider) {
		List<MetaDataProvider> metaDataProviders = newArrayList();
		if ( xmlMetaDataProvider != null ) {
			metaDataProviders.add( xmlMetaDataProvider );
		}

		if ( !constraintMappings.isEmpty() ) {
			metaDataProviders.add(
					new ProgrammaticMetaDataProvider(
							constraintHelper,
							parameterNameProvider,
							constraintMappings
					)
			);
		}
		return metaDataProviders;
	}

	private boolean checkPropertiesForFailFast(Map<String, String> properties, boolean programmaticConfiguredFailFast) {
		boolean failFast = programmaticConfiguredFailFast;
		String failFastPropValue = properties.get( HibernateValidatorConfiguration.FAIL_FAST );
		if ( failFastPropValue != null ) {
			boolean tmpFailFast = Boolean.valueOf( failFastPropValue );
			if ( programmaticConfiguredFailFast && !tmpFailFast ) {
				throw log.getInconsistentFailFastConfigurationException();
			}
			failFast = tmpFailFast;
		}
		return failFast;
	}

	/**
	 * Returns a list with {@link ValidatedValueUnwrapper}s configured via the
	 * {@link HibernateValidatorConfiguration#VALIDATED_VALUE_HANDLERS} property.
	 *
	 * @param properties the properties used to bootstrap the factory
	 *
	 * @return a list with property-configured {@link ValidatedValueUnwrapper}s; May be empty but never {@code null}
	 */
	private static List<ValidatedValueUnwrapper<?>> getPropertyConfiguredValidatedValueHandlers(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		String propertyValue = properties.get( HibernateValidatorConfiguration.VALIDATED_VALUE_HANDLERS );

		if ( propertyValue == null || propertyValue.isEmpty() ) {
			return Collections.emptyList();
		}

		String[] handlerNames = propertyValue.split( "," );
		List<ValidatedValueUnwrapper<?>> handlers = newArrayList( handlerNames.length );

		for ( String handlerName : handlerNames ) {
			@SuppressWarnings("unchecked")
			Class<? extends ValidatedValueUnwrapper<?>> handlerType = (Class<? extends ValidatedValueUnwrapper<?>>)
					run( LoadClass.action( handlerName, externalClassLoader ) );
			handlers.add( run( NewInstance.action( handlerType, "validated value handler class" ) ) );
		}

		return handlers;
	}

	/**
	 * Returns a list with {@link org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor} instances configured via the
	 * {@link HibernateValidatorConfiguration#CONSTRAINT_DEFINITION_CONTRIBUTORS} property.
	 *
	 * @param properties the properties used to bootstrap the factory
	 *
	 * @return a list with property-configured {@link org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor}s. May be empty but never {@code null}.
	 */
	private static List<ConstraintDefinitionContributor> getPropertyConfiguredConstraintDefinitionContributors(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		String propertyValue = properties.get( HibernateValidatorConfiguration.CONSTRAINT_DEFINITION_CONTRIBUTORS );

		if ( propertyValue == null || propertyValue.isEmpty() ) {
			return Collections.emptyList();
		}

		String[] constraintDefinitionContributorNames = propertyValue.split( "," );
		List<ConstraintDefinitionContributor> constraintDefinitionContributors = newArrayList(
				constraintDefinitionContributorNames.length
		);

		for ( String fqcn : constraintDefinitionContributorNames ) {
			@SuppressWarnings("unchecked")
			Class<ConstraintDefinitionContributor> contributorType = (Class<ConstraintDefinitionContributor>)
					run( LoadClass.action( fqcn, externalClassLoader ) );
			constraintDefinitionContributors.add(
					run( NewInstance.action( contributorType, "constraint definition contributor class" ) )
			);
		}

		return constraintDefinitionContributors;
	}

	private static void registerCustomConstraintValidators(ConfigurationImpl hibernateSpecificConfig,
			Map<String, String> properties, ClassLoader externalClassLoader, ConstraintHelper constraintHelper) {
		for ( ConstraintDefinitionContributor contributor : hibernateSpecificConfig.getConstraintDefinitionContributors() ) {
			registerConstraintValidators( contributor, constraintHelper );
		}

		for ( ConstraintDefinitionContributor contributor : getPropertyConfiguredConstraintDefinitionContributors(
				properties, externalClassLoader
		) ) {
			registerConstraintValidators( contributor, constraintHelper );
		}
	}

	private static void registerConstraintValidators(ConstraintDefinitionContributor contributor, ConstraintHelper constraintHelper) {
		ConstraintDefinitionBuilderImpl builder = new ConstraintDefinitionBuilderImpl();
		contributor.collectConstraintDefinitions( builder );

		for ( ConstraintDefinitionContribution<?> constraintDefinitionContribution : builder.getConstraintValidatorContributions() ) {
			processConstraintDefinitionContribution( constraintDefinitionContribution, constraintHelper );
		}
	}

	private static <A extends Annotation> void processConstraintDefinitionContribution(ConstraintDefinitionContribution<A> constraintDefinitionContribution, ConstraintHelper constraintHelper) {
		constraintHelper.putValidatorClasses(
				constraintDefinitionContribution.getConstraintType(),
				constraintDefinitionContribution.getConstraintValidators(),
				constraintDefinitionContribution.keepDefaults()
		);
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

	/**
	 * The one and only {@link ConstraintMappingContributor.ConstraintMappingBuilder} implementation.
	 */
	private static class DefaultConstraintMappingBuilder implements ConstraintMappingContributor.ConstraintMappingBuilder {

		private final Set<DefaultConstraintMapping> mappings = newHashSet();

		@Override
		public ConstraintMapping addConstraintMapping() {
			DefaultConstraintMapping mapping = new DefaultConstraintMapping();
			mappings.add( mapping );
			return mapping;
		}
	}
}
