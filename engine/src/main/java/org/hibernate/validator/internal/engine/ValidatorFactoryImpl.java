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
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration.Builder;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
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
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
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
	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Provider for the current time when validating {@code @Future} or {@code @Past}
	 */
	private final ClockProvider clockProvider;

	/**
	 * The default constraint validator factory for this factory.
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
	 * Hibernate Validator specific flag to abort validation on first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * Hibernate validator specific flags to relax constraints on parameters.
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

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		ClassLoader externalClassLoader = getExternalClassLoader( configurationState );

		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.traversableResolver = configurationState.getTraversableResolver();
		this.parameterNameProvider = new ExecutableParameterNameProvider( configurationState.getParameterNameProvider() );
		this.clockProvider = configurationState.getClockProvider();
		this.valueExtractorManager = new ValueExtractorManager( configurationState.getValueExtractors() );
		this.beanMetaDataManagers = new ConcurrentHashMap<>();
		this.constraintHelper = new ConstraintHelper();
		this.typeResolutionHelper = new TypeResolutionHelper();
		this.executableHelper = executableHelper( configurationState );

		boolean tmpFailFast = false;
		boolean tmpAllowOverridingMethodAlterParameterConstraint = false;
		boolean tmpAllowMultipleCascadedValidationOnReturnValues = false;
		boolean tmpAllowParallelMethodsDefineParameterConstraints = false;

		if ( configurationState instanceof ConfigurationImpl ) {
			ConfigurationImpl hibernateSpecificConfig = (ConfigurationImpl) configurationState;

			// check whether fail fast is programmatically enabled
			tmpFailFast = hibernateSpecificConfig.getFailFast();

			tmpAllowOverridingMethodAlterParameterConstraint =
					hibernateSpecificConfig.getMethodValidationConfiguration()
							.isAllowOverridingMethodAlterParameterConstraint();
			tmpAllowMultipleCascadedValidationOnReturnValues =
					hibernateSpecificConfig.getMethodValidationConfiguration()
							.isAllowMultipleCascadedValidationOnReturnValues();
			tmpAllowParallelMethodsDefineParameterConstraints =
					hibernateSpecificConfig.getMethodValidationConfiguration()
							.isAllowParallelMethodsDefineParameterConstraints();
		}

		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider(
							constraintHelper, typeResolutionHelper, executableHelper, valueExtractorManager, configurationState.getMappingStreams(), externalClassLoader
			);
		}

		this.constraintMappings = Collections.unmodifiableSet(
				getConstraintMappings(
						typeResolutionHelper,
						executableHelper,
						configurationState,
						externalClassLoader
				)
		);

		registerCustomConstraintValidators( constraintMappings, constraintHelper );

		Map<String, String> properties = configurationState.getProperties();

		tmpFailFast = checkPropertiesForBoolean( properties, HibernateValidatorConfiguration.FAIL_FAST, tmpFailFast );
		this.failFast = tmpFailFast;

		Builder methodValidationConfigurationBuilder = new MethodValidationConfiguration.Builder();

		tmpAllowOverridingMethodAlterParameterConstraint = checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE,
				tmpAllowOverridingMethodAlterParameterConstraint
		);
		methodValidationConfigurationBuilder.allowOverridingMethodAlterParameterConstraint(
				tmpAllowOverridingMethodAlterParameterConstraint
		);

		tmpAllowMultipleCascadedValidationOnReturnValues = checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_MULTIPLE_CASCADED_VALIDATION_ON_RESULT,
				tmpAllowMultipleCascadedValidationOnReturnValues
		);
		methodValidationConfigurationBuilder.allowMultipleCascadedValidationOnReturnValues(
				tmpAllowMultipleCascadedValidationOnReturnValues
		);

		tmpAllowParallelMethodsDefineParameterConstraints = checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS,
				tmpAllowParallelMethodsDefineParameterConstraints
		);
		methodValidationConfigurationBuilder.allowParallelMethodsDefineParameterConstraints(
				tmpAllowParallelMethodsDefineParameterConstraints
		);
		this.methodValidationConfiguration = methodValidationConfigurationBuilder.build();

		this.constraintValidatorManager = new ConstraintValidatorManager( configurationState.getConstraintValidatorFactory() );
	}

	private static ClassLoader getExternalClassLoader( ConfigurationState configurationState ) {
		return ( configurationState instanceof ConfigurationImpl ) ? ( (ConfigurationImpl) configurationState ).getExternalClassLoader() : null;
	}

	private static Set<DefaultConstraintMapping> getConstraintMappings(TypeResolutionHelper typeResolutionHelper,
			ExecutableHelper executableHelper,
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
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( constraintMappings, executableHelper );
			serviceLoaderBasedContributor.createConstraintMappings( builder );
		}

		// XML-defined constraint mapping contributors
		List<ConstraintMappingContributor> contributors = getPropertyConfiguredConstraintMappingContributors( configurationState.getProperties(),
						externalClassLoader );

		for ( ConstraintMappingContributor contributor : contributors ) {
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( constraintMappings, executableHelper );
			contributor.createConstraintMappings( builder );
		}

		return constraintMappings;
	}

	@Override
	public Validator getValidator() {
		return createValidator(
				constraintValidatorManager.getDefaultConstraintValidatorFactory(),
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				clockProvider,
				failFast,
				valueExtractorManager,
				methodValidationConfiguration
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
		return parameterNameProvider.getDelegate();
	}

	public ExecutableParameterNameProvider getExecutableParameterNameProvider() {
		return parameterNameProvider;
	}

	@Override
	public ClockProvider getClockProvider() {
		return clockProvider;
	}

	public boolean isFailFast() {
		return failFast;
	}

	MethodValidationConfiguration getMethodValidationConfiguration() {
		return methodValidationConfiguration;
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
		throw log.getTypeNotSupportedForUnwrappingException( type );
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
	}

	Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory,
				    MessageInterpolator messageInterpolator,
	                TraversableResolver traversableResolver,
	                ExecutableParameterNameProvider parameterNameProvider,
	                ClockProvider clockProvider,
	                boolean failFast,
	                ValueExtractorManager valueExtractorManager,
	                MethodValidationConfiguration methodValidationConfiguration ) {

		ValidationOrderGenerator validationOrderGenerator = new ValidationOrderGenerator();

		BeanMetaDataManager beanMetaDataManager = beanMetaDataManagers.computeIfAbsent(
				new BeanMetaDataManagerKey( parameterNameProvider, valueExtractorManager, methodValidationConfiguration ),
				key -> new BeanMetaDataManager(
						constraintHelper,
						executableHelper,
						typeResolutionHelper,
						parameterNameProvider,
						valueExtractorManager,
						validationOrderGenerator,
						buildDataProviders(),
						methodValidationConfiguration
				)
		 );

		return new ValidatorImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				beanMetaDataManager,
				parameterNameProvider,
				clockProvider,
				valueExtractorManager,
				constraintValidatorManager,
				validationOrderGenerator,
				failFast
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

	private boolean checkPropertiesForBoolean(Map<String, String> properties, String propertyKey, boolean programmaticValue) {
		boolean value = programmaticValue;
		String propertyStringValue = properties.get( propertyKey );
		if ( propertyStringValue != null ) {
			boolean configurationValue = Boolean.valueOf( propertyStringValue );
			// throw an exception if the programmatic value is true and it overrides a false configured value
			if ( programmaticValue && !configurationValue ) {
				throw log.getInconsistentFailFastConfigurationException();
			}
			value = configurationValue;
		}
		return value;
	}

	/**
	 * Returns a list with {@link ConstraintMappingContributor}s configured via the
	 * {@link HibernateValidatorConfiguration#CONSTRAINT_MAPPING_CONTRIBUTORS} property.
	 *
	 * @param properties the properties used to bootstrap the factory
	 *
	 * @return a list with property-configured {@link ContraintMappingContributor}s; May be empty but never {@code null}
	 */
	private static List<ConstraintMappingContributor> getPropertyConfiguredConstraintMappingContributors(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		String propertyValue = properties.get( HibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTORS );

		if ( StringHelper.isNullOrEmptyString( propertyValue ) ) {
			return Collections.emptyList();
		}

		String[] contributorNames = propertyValue.toString().split( "," );
		List<ConstraintMappingContributor> contributors = newArrayList( contributorNames.length );

		for ( String contributorName : contributorNames ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintMappingContributor> contributorType = (Class<? extends ConstraintMappingContributor>) run(
					LoadClass.action( contributorName, externalClassLoader ) );
			contributors.add( run( NewInstance.action( contributorType, "constraint mapping contributor class" ) ) );
		}

		return contributors;
	}

	private static ExecutableHelper executableHelper(ConfigurationState state ) {
		ExecutableHelper helper = null;
		if ( state instanceof ConfigurationImpl ) {
			helper = ( (ConfigurationImpl) state ).getExecutableHelper();
		}
		return helper;
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
			throw log.getConstraintHasAlreadyBeenConfiguredViaProgrammaticApiException( constraintType );
		}
		definedConstraints.add( constraintType );
		constraintHelper.putValidatorDescriptors(
				constraintType,
				constraintDefinitionContribution.getValidatorDescriptors(),
				constraintDefinitionContribution.includeExisting()
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
	private static class DefaultConstraintMappingBuilder
			implements ConstraintMappingContributor.ConstraintMappingBuilder {

		private final Set<DefaultConstraintMapping> mappings;
		private final ExecutableHelper executableHelper;

		public DefaultConstraintMappingBuilder(Set<DefaultConstraintMapping> mappings, ExecutableHelper executableHelper) {
			super();
			this.mappings = mappings;
			this.executableHelper = executableHelper;
		}

		@Override
		public ConstraintMapping addConstraintMapping() {
			DefaultConstraintMapping mapping = new DefaultConstraintMapping( executableHelper );
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
}
