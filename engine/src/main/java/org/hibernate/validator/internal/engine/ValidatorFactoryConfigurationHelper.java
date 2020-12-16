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

import jakarta.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.metadata.DefaultBeanMetaDataClassNormalizer;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

final class ValidatorFactoryConfigurationHelper {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private ValidatorFactoryConfigurationHelper() {
	}

	static ClassLoader determineExternalClassLoader(ConfigurationState configurationState) {
		return ( configurationState instanceof AbstractConfigurationImpl )
				? ( (AbstractConfigurationImpl<?>) configurationState ).getExternalClassLoader()
				: null;
	}

	static Set<DefaultConstraintMapping> determineConstraintMappings(TypeResolutionHelper typeResolutionHelper,
			ConfigurationState configurationState, JavaBeanHelper javaBeanHelper, ClassLoader externalClassLoader) {
		Set<DefaultConstraintMapping> constraintMappings = newHashSet();

		if ( configurationState instanceof AbstractConfigurationImpl ) {
			AbstractConfigurationImpl<?> hibernateConfiguration = (AbstractConfigurationImpl<?>) configurationState;

			// programmatic config
			/* We add these first so that constraint mapping created through DefaultConstraintMappingBuilder will take
			 * these programmatically defined mappings into account when checking for constraint definition uniqueness
			 */
			constraintMappings.addAll( hibernateConfiguration.getProgrammaticMappings() );

			// service loader based config
			ConstraintMappingContributor serviceLoaderBasedContributor = new ServiceLoaderBasedConstraintMappingContributor(
					typeResolutionHelper,
					externalClassLoader != null ? externalClassLoader : run( GetClassLoader.fromContext() ) );
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( javaBeanHelper, constraintMappings );
			serviceLoaderBasedContributor.createConstraintMappings( builder );
		}

		// XML-defined constraint mapping contributors
		List<ConstraintMappingContributor> contributors = determinePropertyConfiguredConstraintMappingContributors( configurationState.getProperties(),
				externalClassLoader );

		for ( ConstraintMappingContributor contributor : contributors ) {
			DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder( javaBeanHelper, constraintMappings );
			contributor.createConstraintMappings( builder );
		}

		return constraintMappings;
	}

	static boolean checkPropertiesForBoolean(Map<String, String> properties, String propertyKey, boolean programmaticValue) {
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
	static List<ConstraintMappingContributor> determinePropertyConfiguredConstraintMappingContributors(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		@SuppressWarnings("deprecation")
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

	static boolean determineAllowParallelMethodsDefineParameterConstraints(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowParallelMethodsDefineParameterConstraints() : false
		);
	}

	static boolean determineAllowMultipleCascadedValidationOnReturnValues(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_MULTIPLE_CASCADED_VALIDATION_ON_RESULT,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowMultipleCascadedValidationOnReturnValues() : false
		);
	}

	static boolean determineAllowOverridingMethodAlterParameterConstraint(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ALLOW_PARAMETER_CONSTRAINT_OVERRIDE,
				hibernateSpecificConfig != null ? hibernateSpecificConfig.getMethodValidationConfiguration().isAllowOverridingMethodAlterParameterConstraint() : false
		);
	}

	static boolean determineTraversableResolverResultCacheEnabled(AbstractConfigurationImpl<?> configuration, Map<String, String> properties) {
		return checkPropertiesForBoolean(
				properties,
				HibernateValidatorConfiguration.ENABLE_TRAVERSABLE_RESOLVER_RESULT_CACHE,
				configuration != null ? configuration.isTraversableResolverResultCacheEnabled() : true
		);
	}

	static boolean determineFailFast(AbstractConfigurationImpl<?> configuration, Map<String, String> properties) {
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

	static ScriptEvaluatorFactory determineScriptEvaluatorFactory(ConfigurationState configurationState, Map<String, String> properties,
			ClassLoader externalClassLoader) {
		if ( configurationState instanceof AbstractConfigurationImpl ) {
			AbstractConfigurationImpl<?> hibernateSpecificConfig = (AbstractConfigurationImpl<?>) configurationState;
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

	static Duration determineTemporalValidationTolerance(ConfigurationState configurationState, Map<String, String> properties) {
		if ( configurationState instanceof AbstractConfigurationImpl ) {
			AbstractConfigurationImpl<?> hibernateSpecificConfig = (AbstractConfigurationImpl<?>) configurationState;
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

	static Object determineConstraintValidatorPayload(ConfigurationState configurationState) {
		if ( configurationState instanceof AbstractConfigurationImpl ) {
			AbstractConfigurationImpl<?> hibernateSpecificConfig = (AbstractConfigurationImpl<?>) configurationState;
			if ( hibernateSpecificConfig.getConstraintValidatorPayload() != null ) {
				LOG.logConstraintValidatorPayload( hibernateSpecificConfig.getConstraintValidatorPayload() );
				return hibernateSpecificConfig.getConstraintValidatorPayload();
			}
		}

		return null;
	}

	static ExpressionLanguageFeatureLevel determineConstraintExpressionLanguageFeatureLevel(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getConstraintExpressionLanguageFeatureLevel() != null ) {
			LOG.logConstraintExpressionLanguageFeatureLevel( hibernateSpecificConfig.getConstraintExpressionLanguageFeatureLevel() );
			return ExpressionLanguageFeatureLevel.interpretDefaultForConstraints( hibernateSpecificConfig.getConstraintExpressionLanguageFeatureLevel() );
		}

		String expressionLanguageFeatureLevelName = properties.get( HibernateValidatorConfiguration.CONSTRAINT_EXPRESSION_LANGUAGE_FEATURE_LEVEL );
		if ( expressionLanguageFeatureLevelName != null ) {
			try {
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel = ExpressionLanguageFeatureLevel.of( expressionLanguageFeatureLevelName );
				LOG.logConstraintExpressionLanguageFeatureLevel( expressionLanguageFeatureLevel );
				return ExpressionLanguageFeatureLevel.interpretDefaultForConstraints( expressionLanguageFeatureLevel );
			}
			catch (IllegalArgumentException e) {
				throw LOG.invalidExpressionLanguageFeatureLevelValue( expressionLanguageFeatureLevelName, e );
			}
		}

		return ExpressionLanguageFeatureLevel.interpretDefaultForConstraints( ExpressionLanguageFeatureLevel.DEFAULT );
	}

	static ExpressionLanguageFeatureLevel determineCustomViolationExpressionLanguageFeatureLevel(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getCustomViolationExpressionLanguageFeatureLevel() != null ) {
			LOG.logCustomViolationExpressionLanguageFeatureLevel( hibernateSpecificConfig.getCustomViolationExpressionLanguageFeatureLevel() );
			return ExpressionLanguageFeatureLevel.interpretDefaultForCustomViolations( hibernateSpecificConfig.getCustomViolationExpressionLanguageFeatureLevel() );
		}

		String expressionLanguageFeatureLevelName = properties.get( HibernateValidatorConfiguration.CUSTOM_VIOLATION_EXPRESSION_LANGUAGE_FEATURE_LEVEL );
		if ( expressionLanguageFeatureLevelName != null ) {
			try {
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel = ExpressionLanguageFeatureLevel.of( expressionLanguageFeatureLevelName );
				LOG.logCustomViolationExpressionLanguageFeatureLevel( expressionLanguageFeatureLevel );
				return ExpressionLanguageFeatureLevel.interpretDefaultForCustomViolations( expressionLanguageFeatureLevel );
			}
			catch (IllegalArgumentException e) {
				throw LOG.invalidExpressionLanguageFeatureLevelValue( expressionLanguageFeatureLevelName, e );
			}
		}

		return ExpressionLanguageFeatureLevel.NONE;
	}

	static GetterPropertySelectionStrategy determineGetterPropertySelectionStrategy(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties,
			ClassLoader externalClassLoader) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getGetterPropertySelectionStrategy() != null ) {
			LOG.usingGetterPropertySelectionStrategy( hibernateSpecificConfig.getGetterPropertySelectionStrategy().getClass() );
			return hibernateSpecificConfig.getGetterPropertySelectionStrategy();
		}

		String getterPropertySelectionStrategyFqcn = properties.get( HibernateValidatorConfiguration.GETTER_PROPERTY_SELECTION_STRATEGY_CLASSNAME );
		if ( getterPropertySelectionStrategyFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends GetterPropertySelectionStrategy> clazz = (Class<? extends GetterPropertySelectionStrategy>) run(
						LoadClass.action( getterPropertySelectionStrategyFqcn, externalClassLoader )
				);
				GetterPropertySelectionStrategy getterPropertySelectionStrategy = run( NewInstance.action( clazz, "getter property selection strategy class" ) );
				LOG.usingGetterPropertySelectionStrategy( clazz );

				return getterPropertySelectionStrategy;
			}
			catch (Exception e) {
				throw LOG.getUnableToInstantiateGetterPropertySelectionStrategyClassException( getterPropertySelectionStrategyFqcn, e );
			}
		}

		return new DefaultGetterPropertySelectionStrategy();
	}

	static BeanMetaDataClassNormalizer determineBeanMetaDataClassNormalizer(AbstractConfigurationImpl<?> hibernateSpecificConfig) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getBeanMetaDataClassNormalizer() != null ) {
			return hibernateSpecificConfig.getBeanMetaDataClassNormalizer();
		}

		return new DefaultBeanMetaDataClassNormalizer();
	}

	static PropertyNodeNameProvider determinePropertyNodeNameProvider(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties,
			ClassLoader externalClassLoader) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getPropertyNodeNameProvider() != null ) {
			LOG.usingPropertyNodeNameProvider( hibernateSpecificConfig.getPropertyNodeNameProvider().getClass() );

			return hibernateSpecificConfig.getPropertyNodeNameProvider();
		}

		String propertyNodeNameProviderFqcn = properties.get( HibernateValidatorConfiguration.PROPERTY_NODE_NAME_PROVIDER_CLASSNAME );
		if ( propertyNodeNameProviderFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends PropertyNodeNameProvider> clazz = (Class<? extends PropertyNodeNameProvider>) run( LoadClass.action( propertyNodeNameProviderFqcn, externalClassLoader ) );
				PropertyNodeNameProvider propertyNodeNameProvider = run( NewInstance.action( clazz, "property node name provider class" ) );
				LOG.usingPropertyNodeNameProvider( clazz );

				return propertyNodeNameProvider;
			}
			catch (Exception e) {
				throw LOG.getUnableToInstantiatePropertyNodeNameProviderClassException( propertyNodeNameProviderFqcn, e );
			}
		}

		return new DefaultPropertyNodeNameProvider();
	}

	static LocaleResolver determineLocaleResolver(AbstractConfigurationImpl<?> hibernateSpecificConfig, Map<String, String> properties,
			ClassLoader externalClassLoader) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getLocaleResolver() != null ) {
			LOG.usingLocaleResolver( hibernateSpecificConfig.getLocaleResolver().getClass() );

			return hibernateSpecificConfig.getLocaleResolver();
		}

		String localeResolverFqcn = properties.get( HibernateValidatorConfiguration.LOCALE_RESOLVER_CLASSNAME );
		if ( localeResolverFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends LocaleResolver> clazz = (Class<? extends LocaleResolver>) run( LoadClass.action( localeResolverFqcn, externalClassLoader ) );
				LocaleResolver localeResolver = run( NewInstance.action( clazz, "locale resolver class" ) );
				LOG.usingLocaleResolver( clazz );

				return localeResolver;
			}
			catch (Exception e) {
				throw LOG.getUnableToInstantiateLocaleResolverClassException( localeResolverFqcn, e );
			}
		}

		return new DefaultLocaleResolver();
	}

	static void registerCustomConstraintValidators(Set<DefaultConstraintMapping> constraintMappings,
			ConstraintHelper constraintHelper) {
		Set<Class<?>> definedConstraints = newHashSet();
		for ( DefaultConstraintMapping constraintMapping : constraintMappings ) {
			for ( ConstraintDefinitionContribution<?> contribution : constraintMapping.getConstraintDefinitionContributions() ) {
				processConstraintDefinitionContribution( contribution, constraintHelper, definedConstraints );
			}
		}
	}

	static <A extends Annotation> void processConstraintDefinitionContribution(
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

	static void logValidatorFactoryScopedConfiguration(ValidatorFactoryScopedContext context) {
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

		private final JavaBeanHelper javaBeanHelper;
		private final Set<DefaultConstraintMapping> mappings;

		public DefaultConstraintMappingBuilder(JavaBeanHelper javaBeanHelper, Set<DefaultConstraintMapping> mappings) {
			this.javaBeanHelper = javaBeanHelper;
			this.mappings = mappings;
		}

		@Override
		public ConstraintMapping addConstraintMapping() {
			DefaultConstraintMapping mapping = new DefaultConstraintMapping( javaBeanHelper );
			mappings.add( mapping );
			return mapping;
		}
	}
}
