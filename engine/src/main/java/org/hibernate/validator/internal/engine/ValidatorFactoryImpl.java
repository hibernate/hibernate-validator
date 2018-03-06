/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.internal.util.stereotypes.ThreadSafe;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Factory returning initialized {@code Validator} instances. This is the Hibernate Validator default
 * implementation of the {@code ValidatorFactory} interface.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ValidatorFactoryImpl implements HibernateValidatorFactory {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Context containing all {@link ValidatorFactory} level helpers and configuration properties.
	 */
	private final ValidatorFactoryScopedContext validatorFactoryScopedContext;

	/**
	 * The constraint validator manager for this factory.
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	/**
	 * Programmatic constraints passed via the Hibernate Validator specific API. Empty if there are
	 * no programmatic constraints
	 */
	@Immutable
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
	 * Hibernate Validator specific flags to relax constraints on parameters.
	 */
	private final MethodValidationConfiguration methodValidationConfiguration;

	/**
	 * Metadata provider for XML configuration.
	 */
	private final XmlMetaDataProvider xmlMetaDataProvider;

	/**
	 * Prior to the introduction of {@code ParameterNameProvider} all the bean meta data was static and could be
	 * cached for all created {@code Validator}s. {@code ParameterNameProvider} makes parts of the meta data and
	 * Bean Validation element descriptors dynamic, since depending of the used provider different parameter names
	 * could be used. To still have the metadata static we create a {@code BeanMetaDataManager} per parameter name
	 * provider. See also HV-659.
	 */
	@ThreadSafe
	private final ConcurrentMap<BeanMetaDataManagerKey, BeanMetaDataManager> beanMetaDataManagers;

	private final ValueExtractorManager valueExtractorManager;

	private final ValidationOrderGenerator validationOrderGenerator;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		ClassLoader externalClassLoader = getExternalClassLoader( configurationState );

		this.valueExtractorManager = new ValueExtractorManager( configurationState.getValueExtractors() );
		this.beanMetaDataManagers = new ConcurrentHashMap<>();
		this.constraintHelper = new ConstraintHelper();
		this.typeResolutionHelper = new TypeResolutionHelper();
		this.executableHelper = new ExecutableHelper( typeResolutionHelper );

		ConfigurationImpl hibernateSpecificConfig = null;
		if ( configurationState instanceof ConfigurationImpl ) {
			hibernateSpecificConfig = (ConfigurationImpl) configurationState;
		}

		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider(
					constraintHelper, typeResolutionHelper, valueExtractorManager, configurationState.getMappingStreams(), externalClassLoader
			);
		}

		this.constraintMappings = Collections.unmodifiableSet(
				getConstraintMappings(
						typeResolutionHelper,
						configurationState,
						externalClassLoader
				)
		);

		registerCustomConstraintValidators( constraintMappings, constraintHelper );

		Map<String, String> properties = configurationState.getProperties();

		this.methodValidationConfiguration = new MethodValidationConfiguration.Builder()
				.allowOverridingMethodAlterParameterConstraint(
						getAllowOverridingMethodAlterParameterConstraint( hibernateSpecificConfig, properties )
				).allowMultipleCascadedValidationOnReturnValues(
						getAllowMultipleCascadedValidationOnReturnValues( hibernateSpecificConfig, properties )
				).allowParallelMethodsDefineParameterConstraints(
						getAllowParallelMethodsDefineParameterConstraints( hibernateSpecificConfig, properties )
				).build();

		this.constraintValidatorManager = new ConstraintValidatorManager( configurationState.getConstraintValidatorFactory() );

		this.validatorFactoryScopedContext = new ValidatorFactoryScopedContext(
				configurationState.getMessageInterpolator(),
				configurationState.getTraversableResolver(),
				new ExecutableParameterNameProvider( configurationState.getParameterNameProvider() ),
				configurationState.getClockProvider(),
				getTemporalValidationTolerance( configurationState, properties ),
				getScriptEvaluatorFactory( configurationState, properties, externalClassLoader ),
				getFailFast( hibernateSpecificConfig, properties ),
				getTraversableResolverResultCacheEnabled( hibernateSpecificConfig, properties ),
				getConstraintValidatorPayload( hibernateSpecificConfig )
		);

		this.validationOrderGenerator = new ValidationOrderGenerator();

		if ( LOG.isDebugEnabled() ) {
			logValidatorFactoryScopedConfiguration( validatorFactoryScopedContext );
		}
	}

	private static ClassLoader getExternalClassLoader(ConfigurationState configurationState) {
		return ( configurationState instanceof ConfigurationImpl ) ? ( (ConfigurationImpl) configurationState ).getExternalClassLoader() : null;
	}

	private static Set<DefaultConstraintMapping> getConstraintMappings(TypeResolutionHelper typeResolutionHelper,
			ConfigurationState configurationState, ClassLoader externalClassLoader) {
		Set<DefaultConstraintMapping> constraintMappings = newHashSet();

		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateConfiguration = (ConfigurationImpl) configurationState;

			// programmatic config
			/* We add these first so that constraint mapping created through DefaultConstraintMappingBuilder will take
			 * these programmatically defined mappings into account when checking for constraint definition uniqueness
			 */
			constraintMappings.addAll( hibernateConfiguration.getProgrammaticMappings() );

			// service loader based config
			ConstraintMappingContributor serviceLoaderBasedContributor = new ServiceLoaderBasedConstraintMappingContributor(
					typeResolutionHelper,
					externalClassLoader != null ? externalClassLoader : run( GetClassLoader.fromContext() ) );
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( constraintMappings );
			serviceLoaderBasedContributor.createConstraintMappings( builder );
		}

		// XML-defined constraint mapping contributors
		List<ConstraintMappingContributor> contributors = getPropertyConfiguredConstraintMappingContributors( configurationState.getProperties(),
				externalClassLoader );

		for ( ConstraintMappingContributor contributor : contributors ) {
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( constraintMappings );
			contributor.createConstraintMappings( builder );
		}

		return constraintMappings;
	}

	@Override
	public Validator getValidator() {
		return createValidator(
				constraintValidatorManager.getDefaultConstraintValidatorFactory(),
				valueExtractorManager,
				validatorFactoryScopedContext,
				methodValidationConfiguration
		);
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return validatorFactoryScopedContext.getMessageInterpolator();
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return validatorFactoryScopedContext.getTraversableResolver();
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorManager.getDefaultConstraintValidatorFactory();
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return validatorFactoryScopedContext.getParameterNameProvider().getDelegate();
	}

	public ExecutableParameterNameProvider getExecutableParameterNameProvider() {
		return validatorFactoryScopedContext.getParameterNameProvider();
	}

	@Override
	public ClockProvider getClockProvider() {
		return validatorFactoryScopedContext.getClockProvider();
	}

	@Override
	public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
		return validatorFactoryScopedContext.getScriptEvaluatorFactory();
	}

	@Override
	public Duration getTemporalValidationTolerance() {
		return validatorFactoryScopedContext.getTemporalValidationTolerance();
	}

	public boolean isFailFast() {
		return validatorFactoryScopedContext.isFailFast();
	}

	MethodValidationConfiguration getMethodValidationConfiguration() {
		return methodValidationConfiguration;
	}

	public boolean isTraversableResolverResultCacheEnabled() {
		return validatorFactoryScopedContext.isTraversableResolverResultCacheEnabled();
	}

	ValueExtractorManager getValueExtractorManager() {
		return valueExtractorManager;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateValidatorFactory.class ) ) {
			return type.cast( this );
		}
		throw LOG.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateValidatorContext usingContext() {
		return new ValidatorContextImpl( this );
	}

	@Override
	public void close() {
		constraintValidatorManager.clear();
		constraintHelper.clear();
		for ( BeanMetaDataManager beanMetaDataManager : beanMetaDataManagers.values() ) {
			beanMetaDataManager.clear();
		}
		validatorFactoryScopedContext.getScriptEvaluatorFactory().clear();
		valueExtractorManager.clear();
	}

	public ValidatorFactoryScopedContext getValidatorFactoryScopedContext() {
		return this.validatorFactoryScopedContext;
	}

	Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory,
			ValueExtractorManager valueExtractorManager,
			ValidatorFactoryScopedContext validatorFactoryScopedContext,
			MethodValidationConfiguration methodValidationConfiguration) {

		BeanMetaDataManager beanMetaDataManager = beanMetaDataManagers.computeIfAbsent(
				new BeanMetaDataManagerKey( validatorFactoryScopedContext.getParameterNameProvider(), valueExtractorManager, methodValidationConfiguration ),
				key -> new BeanMetaDataManager(
						constraintHelper,
						executableHelper,
						typeResolutionHelper,
						validatorFactoryScopedContext.getParameterNameProvider(),
						valueExtractorManager,
						validationOrderGenerator,
						buildDataProviders(),
						methodValidationConfiguration
				)
		 );

		return new ValidatorImpl(
				constraintValidatorFactory,
				beanMetaDataManager,
				valueExtractorManager,
				constraintValidatorManager,
				validationOrderGenerator,
				validatorFactoryScopedContext
		);
	}

	private List<MetaDataProvider> buildDataProviders() {
		List<MetaDataProvider> metaDataProviders = newArrayList();
		if ( xmlMetaDataProvider != null ) {
			metaDataProviders.add( xmlMetaDataProvider );
		}

		if ( !constraintMappings.isEmpty() ) {
			metaDataProviders.add(
					new ProgrammaticMetaDataProvider(
							constraintHelper,
							typeResolutionHelper,
							valueExtractorManager,
							constraintMappings
					)
			);
		}
		return metaDataProviders;
	}

	private static boolean checkPropertiesForBoolean(Map<String, String> properties, String propertyKey, boolean programmaticValue) {
		boolean value = programmaticValue;
		String propertyStringValue = properties.get( propertyKey );
		if ( propertyStringValue != null ) {
			value = Boolean.valueOf( propertyStringValue );
		}
		return value;
	}

	/**
	 * Returns a list with {@link ConstraintMappingContributor}s configured via the
	 * {@link HibernateValidatorConfiguration#CONSTRAINT_MAPPING_CONTRIBUTORS} property.
	 *
	 * Also takes into account the deprecated {@link HibernateValidatorConfiguration#CONSTRAINT_MAPPING_CONTRIBUTOR}
	 * property.
	 *
	 * @param properties the properties used to bootstrap the factory
	 *
	 * @return a list with property-configured {@link ConstraintMappingContributor}s; May be empty but never {@code null}
	 */
	private static List<ConstraintMappingContributor> getPropertyConfiguredConstraintMappingContributors(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		String deprecatedPropertyValue = properties.get( HibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTOR );
		String propertyValue = properties.get( HibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTORS );

		if ( StringHelper.isNullOrEmptyString( deprecatedPropertyValue ) && StringHelper.isNullOrEmptyString( propertyValue ) ) {
			return Collections.emptyList();
		}

		StringBuilder assembledPropertyValue = new StringBuilder();
		if ( !StringHelper.isNullOrEmptyString( deprecatedPropertyValue ) ) {
			assembledPropertyValue.append( deprecatedPropertyValue );
		}
		if ( !StringHelper.isNullOrEmptyString( propertyValue ) ) {
			if ( assembledPropertyValue.length() > 0 ) {
				assembledPropertyValue.append( "," );
			}
			assembledPropertyValue.append( propertyValue );
		}

		String[] contributorNames = assembledPropertyValue.toString().split( "," );
		List<ConstraintMappingContributor> contributors = newArrayList( contributorNames.length );

		for ( String contributorName : contributorNames ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintMappingContributor> contributorType = (Class<? extends ConstraintMappingContributor>) run(
					LoadClass.action( contributorName, externalClassLoader ) );
			contributors.add( run( NewInstance.action( contributorType, "constraint mapping contributor class" ) ) );
		}

		return contributors;
	}

	private static boolean getAllowParallelMethodsDefineParameterConstraints(ConfigurationImpl hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowParallelMethodsDefineParameterConstraints() : false
		);
	}

	private static boolean getAllowMultipleCascadedValidationOnReturnValues(ConfigurationImpl hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_MULTIPLE_CASCADED_VALIDATION_ON_RESULT,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowMultipleCascadedValidationOnReturnValues() : false
		);
	}

	private static boolean getAllowOverridingMethodAlterParameterConstraint(ConfigurationImpl hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowOverridingMethodAlterParameterConstraint() : false
		);
	}

	private static boolean getTraversableResolverResultCacheEnabled(ConfigurationImpl configuration, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ENABLE_TRAVERSABLE_RESOLVER_RESULT_CACHE,
				configuration != null ? configuration.isTraversableResolverResultCacheEnabled() : true
		);
	}

	private static boolean getFailFast(ConfigurationImpl configuration, Map<String, String> properties) {
		// check whether fail fast is programmatically enabled
		boolean tmpFailFast = configuration != null ? configuration.getFailFast() : false;

		String propertyStringValue = properties.get( HibernateValidatorConfiguration.FAIL_FAST );
		if ( propertyStringValue != null ) {
			boolean configurationValue = Boolean.valueOf( propertyStringValue );
			// throw an exception if the programmatic value is true and it overrides a false configured value
			if ( tmpFailFast && !configurationValue ) {
				throw LOG.getInconsistentFailFastConfigurationException();
			}
			tmpFailFast = configurationValue;
		}

		return tmpFailFast;
	}

	private static ScriptEvaluatorFactory getScriptEvaluatorFactory(ConfigurationState configurationState, Map<String, String> properties,
			ClassLoader externalClassLoader) {
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;
			if ( hibernateSpecificConfig.getScriptEvaluatorFactory() != null ) {
				LOG.usingScriptEvaluatorFactory( hibernateSpecificConfig.getScriptEvaluatorFactory().getClass() );
				return hibernateSpecificConfig.getScriptEvaluatorFactory();
			}
		}

		String scriptEvaluatorFactoryFqcn = properties.get( HibernateValidatorConfiguration.SCRIPT_EVALUATOR_FACTORY_CLASSNAME );
		if ( scriptEvaluatorFactoryFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ScriptEvaluatorFactory> clazz = (Class<? extends ScriptEvaluatorFactory>) run(
						LoadClass.action( scriptEvaluatorFactoryFqcn, externalClassLoader )
				);
				ScriptEvaluatorFactory scriptEvaluatorFactory = run( NewInstance.action( clazz, "script evaluator factory class" ) );
				LOG.usingScriptEvaluatorFactory( clazz );

				return scriptEvaluatorFactory;
			}
			catch (Exception e) {
				throw LOG.getUnableToInstantiateScriptEvaluatorFactoryClassException( scriptEvaluatorFactoryFqcn, e );
			}
		}

		return new DefaultScriptEvaluatorFactory( externalClassLoader );
	}

	private Duration getTemporalValidationTolerance(ConfigurationState configurationState, Map<String, String> properties) {
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;
			if ( hibernateSpecificConfig.getTemporalValidationTolerance() != null ) {
				LOG.logTemporalValidationTolerance( hibernateSpecificConfig.getTemporalValidationTolerance() );
				return hibernateSpecificConfig.getTemporalValidationTolerance();
			}
		}
		String temporalValidationToleranceProperty = properties.get( HibernateValidatorConfiguration.TEMPORAL_VALIDATION_TOLERANCE );
		if ( temporalValidationToleranceProperty != null ) {
			try {
				Duration tolerance = Duration.ofMillis( Long.parseLong( temporalValidationToleranceProperty ) ).abs();
				LOG.logTemporalValidationTolerance( tolerance );
				return tolerance;
			}
			catch (Exception e) {
				throw LOG.getUnableToParseTemporalValidationToleranceException( temporalValidationToleranceProperty, e );
			}
		}

		return Duration.ZERO;
	}

	private Object getConstraintValidatorPayload(ConfigurationState configurationState) {
		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;
			if ( hibernateSpecificConfig.getConstraintValidatorPayload() != null ) {
				LOG.logConstraintValidatorPayload( hibernateSpecificConfig.getConstraintValidatorPayload() );
				return hibernateSpecificConfig.getConstraintValidatorPayload();
			}
		}

		return null;
	}

	private static void registerCustomConstraintValidators(Set<DefaultConstraintMapping> constraintMappings,
			ConstraintHelper constraintHelper) {
		Set<Class<?>> definedConstraints = newHashSet();
		for ( DefaultConstraintMapping constraintMapping : constraintMappings ) {
			for ( ConstraintDefinitionContribution<?> contribution : constraintMapping.getConstraintDefinitionContributions() ) {
				processConstraintDefinitionContribution( contribution, constraintHelper, definedConstraints );
			}
		}
	}

	private static <A extends Annotation> void processConstraintDefinitionContribution(
			ConstraintDefinitionContribution<A> constraintDefinitionContribution, ConstraintHelper constraintHelper,
			Set<Class<?>> definedConstraints) {
		Class<A> constraintType = constraintDefinitionContribution.getConstraintType();
		if ( definedConstraints.contains( constraintType ) ) {
			throw LOG.getConstraintHasAlreadyBeenConfiguredViaProgrammaticApiException( constraintType );
		}
		definedConstraints.add( constraintType );
		constraintHelper.putValidatorDescriptors(
				constraintType,
				constraintDefinitionContribution.getValidatorDescriptors(),
				constraintDefinitionContribution.includeExisting()
		);
	}

	private static void logValidatorFactoryScopedConfiguration(ValidatorFactoryScopedContext context) {
		LOG.logValidatorFactoryScopedConfiguration( context.getMessageInterpolator().getClass(), "message interpolator" );
		LOG.logValidatorFactoryScopedConfiguration( context.getTraversableResolver().getClass(), "traversable resolver" );
		LOG.logValidatorFactoryScopedConfiguration( context.getParameterNameProvider().getClass(), "parameter name provider" );
		LOG.logValidatorFactoryScopedConfiguration( context.getClockProvider().getClass(), "clock provider" );
		LOG.logValidatorFactoryScopedConfiguration( context.getScriptEvaluatorFactory().getClass(), "script evaluator factory" );
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
	private static class DefaultConstraintMappingBuilder
			implements ConstraintMappingContributor.ConstraintMappingBuilder {
		private final Set<DefaultConstraintMapping> mappings;

		public DefaultConstraintMappingBuilder(Set<DefaultConstraintMapping> mappings) {
			super();
			this.mappings = mappings;
		}

		@Override
		public ConstraintMapping addConstraintMapping() {
			DefaultConstraintMapping mapping = new DefaultConstraintMapping();
			mappings.add( mapping );
			return mapping;
		}
	}

	private static class BeanMetaDataManagerKey {
		private final ExecutableParameterNameProvider parameterNameProvider;
		private final ValueExtractorManager valueExtractorManager;
		private final MethodValidationConfiguration methodValidationConfiguration;
		private final int hashCode;

		public BeanMetaDataManagerKey(ExecutableParameterNameProvider parameterNameProvider, ValueExtractorManager valueExtractorManager, MethodValidationConfiguration methodValidationConfiguration) {
			this.parameterNameProvider = parameterNameProvider;
			this.valueExtractorManager = valueExtractorManager;
			this.methodValidationConfiguration = methodValidationConfiguration;
			this.hashCode = buildHashCode( parameterNameProvider, valueExtractorManager, methodValidationConfiguration );
		}

		private static int buildHashCode(ExecutableParameterNameProvider parameterNameProvider, ValueExtractorManager valueExtractorManager, MethodValidationConfiguration methodValidationConfiguration) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( methodValidationConfiguration == null ) ? 0 : methodValidationConfiguration.hashCode() );
			result = prime * result + ( ( parameterNameProvider == null ) ? 0 : parameterNameProvider.hashCode() );
			result = prime * result + ( ( valueExtractorManager == null ) ? 0 : valueExtractorManager.hashCode() );
			return result;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			BeanMetaDataManagerKey other = (BeanMetaDataManagerKey) obj;

			return methodValidationConfiguration.equals( other.methodValidationConfiguration ) &&
					parameterNameProvider.equals( other.parameterNameProvider ) &&
					valueExtractorManager.equals( other.valueExtractorManager );
		}

		@Override
		public String toString() {
			return "BeanMetaDataManagerKey [parameterNameProvider=" + parameterNameProvider + ", valueExtractorManager=" + valueExtractorManager
					+ ", methodValidationConfiguration=" + methodValidationConfiguration + "]";
		}
	}

	static class ValidatorFactoryScopedContext {
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
		private final ExecutableParameterNameProvider parameterNameProvider;

		/**
		 * Provider for the current time when validating {@code @Future} or {@code @Past}
		 */
		private final ClockProvider clockProvider;

		/**
		 * Defines the temporal validation tolerance i.e. the allowed margin of error when comparing date/time in temporal
		 * constraints.
		 */
		private final Duration temporalValidationTolerance;

		/**
		 * Used to get the {@code ScriptEvaluatorFactory} when validating {@code @ScriptAssert} and
		 * {@code @ParameterScriptAssert} constraints.
		 */
		private final ScriptEvaluatorFactory scriptEvaluatorFactory;

		/**
		 * Hibernate Validator specific flag to abort validation on first constraint violation.
		 */
		private final boolean failFast;

		/**
		 * Hibernate Validator specific flag to disable the {@code TraversableResolver} result cache.
		 */
		private final boolean traversableResolverResultCacheEnabled;

		private final Object constraintValidatorPayload;

		private ValidatorFactoryScopedContext(MessageInterpolator messageInterpolator,
				TraversableResolver traversableResolver,
				ExecutableParameterNameProvider parameterNameProvider,
				ClockProvider clockProvider,
				Duration temporalValidationTolerance,
				ScriptEvaluatorFactory scriptEvaluatorFactory,
				boolean failFast,
				boolean traversableResolverResultCacheEnabled,
				Object constraintValidatorPayload) {
			this.messageInterpolator = messageInterpolator;
			this.traversableResolver = traversableResolver;
			this.parameterNameProvider = parameterNameProvider;
			this.clockProvider = clockProvider;
			this.temporalValidationTolerance = temporalValidationTolerance;
			this.scriptEvaluatorFactory = scriptEvaluatorFactory;
			this.failFast = failFast;
			this.traversableResolverResultCacheEnabled = traversableResolverResultCacheEnabled;
			this.constraintValidatorPayload = constraintValidatorPayload;
		}

		public MessageInterpolator getMessageInterpolator() {
			return this.messageInterpolator;
		}

		public TraversableResolver getTraversableResolver() {
			return this.traversableResolver;
		}

		public ExecutableParameterNameProvider getParameterNameProvider() {
			return this.parameterNameProvider;
		}

		public ClockProvider getClockProvider() {
			return this.clockProvider;
		}

		public Duration getTemporalValidationTolerance() {
			return this.temporalValidationTolerance;
		}

		public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
			return this.scriptEvaluatorFactory;
		}

		public boolean isFailFast() {
			return this.failFast;
		}

		public boolean isTraversableResolverResultCacheEnabled() {
			return this.traversableResolverResultCacheEnabled;
		}

		public Object getConstraintValidatorPayload() {
			return this.constraintValidatorPayload;
		}

		static class Builder {
			private final ValidatorFactoryScopedContext defaultContext;

			private MessageInterpolator messageInterpolator;
			private TraversableResolver traversableResolver;
			private ExecutableParameterNameProvider parameterNameProvider;
			private ClockProvider clockProvider;
			private ScriptEvaluatorFactory scriptEvaluatorFactory;
			private Duration temporalValidationTolerance;
			private boolean failFast;
			private boolean traversableResolverResultCacheEnabled;
			private Object constraintValidatorPayload;

			Builder(ValidatorFactoryScopedContext defaultContext) {
				Contracts.assertNotNull( defaultContext, "Default context cannot be null." );

				this.defaultContext = defaultContext;
				this.messageInterpolator = defaultContext.messageInterpolator;
				this.traversableResolver = defaultContext.traversableResolver;
				this.parameterNameProvider = defaultContext.parameterNameProvider;
				this.clockProvider = defaultContext.clockProvider;
				this.scriptEvaluatorFactory = defaultContext.scriptEvaluatorFactory;
				this.temporalValidationTolerance = defaultContext.temporalValidationTolerance;
				this.failFast = defaultContext.failFast;
				this.traversableResolverResultCacheEnabled = defaultContext.traversableResolverResultCacheEnabled;
				this.constraintValidatorPayload = defaultContext.constraintValidatorPayload;
			}

			public Builder setMessageInterpolator(MessageInterpolator messageInterpolator) {
				if ( messageInterpolator == null ) {
					this.messageInterpolator = defaultContext.messageInterpolator;
				}
				else {
					this.messageInterpolator = messageInterpolator;
				}

				return this;
			}

			public Builder setTraversableResolver(TraversableResolver traversableResolver) {
				if ( traversableResolver == null ) {
					this.traversableResolver = defaultContext.traversableResolver;
				}
				else {
					this.traversableResolver = traversableResolver;
				}
				return this;
			}

			public Builder setParameterNameProvider(ParameterNameProvider parameterNameProvider) {
				if ( parameterNameProvider == null ) {
					this.parameterNameProvider = defaultContext.parameterNameProvider;
				}
				else {
					this.parameterNameProvider = new ExecutableParameterNameProvider( parameterNameProvider );
				}
				return this;
			}

			public Builder setClockProvider(ClockProvider clockProvider) {
				if ( clockProvider == null ) {
					this.clockProvider = defaultContext.clockProvider;
				}
				else {
					this.clockProvider = clockProvider;
				}
				return this;
			}

			public Builder setTemporalValidationTolerance(Duration temporalValidationTolerance) {
				this.temporalValidationTolerance = temporalValidationTolerance == null ? Duration.ZERO : temporalValidationTolerance.abs();
				return this;
			}

			public Builder setScriptEvaluatorFactory(ScriptEvaluatorFactory scriptEvaluatorFactory) {
				if ( scriptEvaluatorFactory == null ) {
					this.scriptEvaluatorFactory = defaultContext.scriptEvaluatorFactory;
				}
				else {
					this.scriptEvaluatorFactory = scriptEvaluatorFactory;
				}
				return this;
			}

			public Builder setFailFast(boolean failFast) {
				this.failFast = failFast;
				return this;
			}

			public Builder setTraversableResolverResultCacheEnabled(boolean traversableResolverResultCacheEnabled) {
				this.traversableResolverResultCacheEnabled = traversableResolverResultCacheEnabled;
				return this;
			}

			public Builder setConstraintValidatorPayload(Object constraintValidatorPayload) {
				if ( constraintValidatorPayload == null ) {
					this.constraintValidatorPayload = defaultContext.constraintValidatorPayload;
				}
				else {
					this.constraintValidatorPayload = constraintValidatorPayload;
				}
				return this;
			}

			public ValidatorFactoryScopedContext build() {
				return new ValidatorFactoryScopedContext(
						messageInterpolator,
						traversableResolver,
						parameterNameProvider,
						clockProvider,
						temporalValidationTolerance,
						scriptEvaluatorFactory,
						failFast,
						traversableResolverResultCacheEnabled,
						constraintValidatorPayload
				);
			}
		}
	}
}
