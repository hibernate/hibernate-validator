/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.bean.BeanResolverImpl;
import org.hibernate.validator.internal.engine.bean.HibernateValidatorBuiltinBeanConfigurer;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.engine.constraintvalidation.HibernateConstraintValidatorInitializationSharedDataManager;
import org.hibernate.validator.internal.engine.messageinterpolation.DefaultLocaleResolver;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.metadata.DefaultBeanMetaDataClassNormalizer;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.GetInstancesFromServiceLoader;
import org.hibernate.validator.internal.util.actions.LoadClass;
import org.hibernate.validator.internal.util.actions.NewInstance;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.bean.BeanConfigurer;
import org.hibernate.validator.spi.bean.BeanProvider;
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

	static Set<DefaultConstraintMapping> determineServiceLoadedConstraintMappings(
			TypeResolutionHelper typeResolutionHelper,
			JavaBeanHelper javaBeanHelper, ClassLoader externalClassLoader) {
		Set<DefaultConstraintMapping> constraintMappings = newHashSet();

		// service loader based config
		ConstraintMappingContributor serviceLoaderBasedContributor = new ServiceLoaderBasedConstraintMappingContributor(
				typeResolutionHelper,
				externalClassLoader != null ? externalClassLoader : GetClassLoader.fromContext()
		);
		DefaultConstraintMappingBuilder builder = new DefaultConstraintMappingBuilder(
				javaBeanHelper, constraintMappings );
		serviceLoaderBasedContributor.createConstraintMappings( builder );
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
	 * @param properties the properties used to bootstrap the factory
	 *
	 * @return a list with property-configured {@link ConstraintMappingContributor}s; May be empty but never {@code null}
	 */
	static List<ConstraintMappingContributor> determinePropertyConfiguredConstraintMappingContributors(
			Map<String, String> properties, ClassLoader externalClassLoader) {
		String propertyValue = properties.get( HibernateValidatorConfiguration.CONSTRAINT_MAPPING_CONTRIBUTORS );

		if ( StringHelper.isNullOrEmptyString( propertyValue ) ) {
			return Collections.emptyList();
		}

		String[] contributorNames = propertyValue.split( "," );
		List<ConstraintMappingContributor> contributors = newArrayList( contributorNames.length );

		for ( String contributorName : contributorNames ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintMappingContributor> contributorType = (Class<? extends ConstraintMappingContributor>) LoadClass.action( contributorName, externalClassLoader );
			contributors.add( NewInstance.action( contributorType, "constraint mapping contributor class" ) );
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

	static boolean determineFailFastOnPropertyViolation(AbstractConfigurationImpl<?> configuration, Map<String, String> properties) {
		// check whether fail fast on property violation is programmatically enabled
		boolean tmpFailFastOnPropertyViolation = configuration != null ? configuration.getFailFastOnPropertyViolation() : false;

		String propertyStringValue = properties.get( HibernateValidatorConfiguration.FAIL_FAST_ON_PROPERTY_VIOLATION );
		if ( propertyStringValue != null ) {
			boolean configurationValue = Boolean.valueOf( propertyStringValue );
			// throw an exception if the programmatic value is true and it overrides a false configured value
			if ( tmpFailFastOnPropertyViolation && !configurationValue ) {
				throw LOG.getInconsistentFailFastOnPropertyViolationConfigurationException();
			}
			tmpFailFastOnPropertyViolation = configurationValue;
		}

		return tmpFailFastOnPropertyViolation;
	}

	/**
	 * Resolves a factory-scoped infrastructure bean that must be available at construction time.
	 * <p>
	 * These beans are resolved eagerly and cannot benefit from lazy {@link BeanHolder} resolution,
	 * so property-based resolution uses {@link BeanRetrieval#CONSTRUCTOR} (reflection only) to avoid
	 * going through the {@link org.hibernate.validator.spi.bean.BeanProvider}.
	 * <p>
	 * For beans that can be resolved lazily (e.g. {@code MessageInterpolator}, {@code ScriptEvaluatorFactory}),
	 * use {@link #resolveBeanComponentHolder} instead.
	 */
	static <T> T resolveBeanComponent(Class<T> beanType, String defaultName, String propertyKey,
			AbstractConfigurationImpl<?> config, Function<AbstractConfigurationImpl<?>, T> getter,
			Map<String, String> properties, BeanResolver beanResolver) {
		if ( config != null && getter != null ) {
			T val = getter.apply( config );
			if ( val != null ) {
				return val;
			}
		}
		if ( propertyKey != null ) {
			String prop = properties.get( propertyKey );
			if ( prop != null ) {
				return beanResolver.resolve( beanType, prop, BeanRetrieval.CONSTRUCTOR ).get();
			}
		}
		return beanResolver.resolve( beanType, defaultName, BeanRetrieval.BUILTIN ).get();
	}

	static ScriptEvaluatorFactory determineScriptEvaluatorFactory(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties, BeanResolver beanResolver) {
		return resolveBeanComponent( ScriptEvaluatorFactory.class, DefaultScriptEvaluatorFactory.NAME,
				HibernateValidatorConfiguration.SCRIPT_EVALUATOR_FACTORY_CLASSNAME,
				hibernateSpecificConfig, AbstractConfigurationImpl::getScriptEvaluatorFactory,
				properties, beanResolver );
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
		if ( configurationState instanceof AbstractConfigurationImpl<?> hibernateSpecificConfig ) {
			if ( hibernateSpecificConfig.getConstraintValidatorPayload() != null ) {
				LOG.logConstraintValidatorPayload( hibernateSpecificConfig.getConstraintValidatorPayload() );
				return hibernateSpecificConfig.getConstraintValidatorPayload();
			}
		}

		return null;
	}

	static HibernateConstraintValidatorInitializationSharedDataManager initializeConstraintValidatorInitializationShareDataManager(ConfigurationState configurationState) {
		HibernateConstraintValidatorInitializationSharedDataManager configured = null;
		if ( configurationState instanceof AbstractConfigurationImpl<?> hibernateSpecificConfig ) {
			if ( hibernateSpecificConfig.getSharedDataManager() != null ) {
				configured = hibernateSpecificConfig.getSharedDataManager();
			}
		}
		if ( configured == null ) {
			configured = new HibernateConstraintValidatorInitializationSharedDataManager();
		}

		return configured.copy();
	}

	static BeanResolver initializeBeanResolver(ConfigurationState configurationState) {
		List<BeanConfigurer> configurers = new ArrayList<>();
		BeanProvider beanProvider = null;

		ClassLoader classLoader = determineExternalClassLoader( configurationState );
		if ( classLoader == null ) {
			classLoader = Thread.currentThread().getContextClassLoader();
			if ( classLoader == null ) {
				classLoader = ValidatorFactoryConfigurationHelper.class.getClassLoader();
			}
		}

		// 1. Built-in defaults (lowest priority)
		configurers.add( new HibernateValidatorBuiltinBeanConfigurer() );

		// 2. ServiceLoader-discovered configurers
		configurers.addAll( GetInstancesFromServiceLoader.action( classLoader, BeanConfigurer.class ) );

		// 3. User-provided configurers (highest priority)
		if ( configurationState instanceof AbstractConfigurationImpl<?> hibernateSpecificConfig ) {
			configurers.addAll( hibernateSpecificConfig.getBeanConfigurers() );
			beanProvider = hibernateSpecificConfig.getBeanProvider();
		}

		// 4. Default MessageInterpolator bean (captures config state, resolves LocaleResolver through beans)
		final ClassLoader resolvedClassLoader = classLoader;
		if ( configurationState instanceof AbstractConfigurationImpl<?> hibernateSpecificConfig ) {
			final Set<Locale> supportedLocales = hibernateSpecificConfig.getAllSupportedLocales();
			final Locale defaultLocale = hibernateSpecificConfig.getDefaultLocale();
			final boolean preload = hibernateSpecificConfig.preloadResourceBundles();
			final Map<String, String> properties = hibernateSpecificConfig.getProperties();

			configurers.add( context -> context.define( MessageInterpolator.class, "default",
					resolver -> {
						LocaleResolver localeResolver = resolveBeanComponent( LocaleResolver.class, DefaultLocaleResolver.NAME,
								HibernateValidatorConfiguration.LOCALE_RESOLVER_CLASSNAME,
								hibernateSpecificConfig, AbstractConfigurationImpl::getLocaleResolver,
								properties, resolver );
						PlatformResourceBundleLocator userResourceBundleLocator = new PlatformResourceBundleLocator(
								ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES,
								preload ? supportedLocales : Collections.emptySet(),
								resolvedClassLoader
						);
						PlatformResourceBundleLocator contributorResourceBundleLocator = new PlatformResourceBundleLocator(
								ResourceBundleMessageInterpolator.CONTRIBUTOR_VALIDATION_MESSAGES,
								preload ? supportedLocales : Collections.emptySet(),
								resolvedClassLoader,
								true
						);
						return BeanHolder.of( new ResourceBundleMessageInterpolator(
								userResourceBundleLocator,
								contributorResourceBundleLocator,
								supportedLocales,
								defaultLocale,
								localeResolver,
								preload
						) );
					}
			) );
		}

		return BeanResolverImpl.create( classLoader, configurers, beanProvider );
	}

	static MessageInterpolator determineMessageInterpolator(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			ConfigurationState configurationState, Map<String, String> properties, BeanResolver beanResolver) {
		if ( hibernateSpecificConfig == null || hibernateSpecificConfig.isMessageInterpolatorExplicitlySet() ) {
			return configurationState.getMessageInterpolator();
		}
		String prop = properties.get( HibernateValidatorConfiguration.MESSAGE_INTERPOLATOR );
		if ( prop != null ) {
			return beanResolver.resolve( MessageInterpolator.class, prop, BeanRetrieval.CONSTRUCTOR ).get();
		}
		return beanResolver.resolve( MessageInterpolator.class, "default", BeanRetrieval.BUILTIN ).get();
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

	static GetterPropertySelectionStrategy determineGetterPropertySelectionStrategy(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties, BeanResolver beanResolver) {
		return resolveBeanComponent( GetterPropertySelectionStrategy.class, DefaultGetterPropertySelectionStrategy.NAME,
				HibernateValidatorConfiguration.GETTER_PROPERTY_SELECTION_STRATEGY_CLASSNAME,
				hibernateSpecificConfig, AbstractConfigurationImpl::getGetterPropertySelectionStrategy,
				properties, beanResolver );
	}

	static BeanMetaDataClassNormalizer determineBeanMetaDataClassNormalizer(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties, BeanResolver beanResolver) {
		return resolveBeanComponent( BeanMetaDataClassNormalizer.class, DefaultBeanMetaDataClassNormalizer.NAME,
				HibernateValidatorConfiguration.BEAN_META_DATA_CLASS_NORMALIZER,
				hibernateSpecificConfig, AbstractConfigurationImpl::getBeanMetaDataClassNormalizer,
				properties, beanResolver );
	}

	static PropertyNodeNameProvider determinePropertyNodeNameProvider(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties, BeanResolver beanResolver) {
		return resolveBeanComponent( PropertyNodeNameProvider.class, DefaultPropertyNodeNameProvider.NAME,
				HibernateValidatorConfiguration.PROPERTY_NODE_NAME_PROVIDER_CLASSNAME,
				hibernateSpecificConfig, AbstractConfigurationImpl::getPropertyNodeNameProvider,
				properties, beanResolver );
	}

	static LocaleResolver determineLocaleResolver(AbstractConfigurationImpl<?> hibernateSpecificConfig,
			Map<String, String> properties, ClassLoader externalClassLoader) {
		if ( hibernateSpecificConfig != null && hibernateSpecificConfig.getLocaleResolver() != null ) {
			LOG.usingLocaleResolver( hibernateSpecificConfig.getLocaleResolver().getClass() );

			return hibernateSpecificConfig.getLocaleResolver();
		}

		String localeResolverFqcn = properties.get( HibernateValidatorConfiguration.LOCALE_RESOLVER_CLASSNAME );
		if ( localeResolverFqcn != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends LocaleResolver> clazz = (Class<? extends LocaleResolver>) LoadClass.action( localeResolverFqcn, externalClassLoader );
				LocaleResolver localeResolver = NewInstance.action( clazz, "locale resolver class" );
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

	static boolean determineShowValidatedValuesInTraceLogs(AbstractConfigurationImpl<?> configuration, Map<String, String> properties) {
		// check whether showing the validation values in trace logs is programmatically enabled
		boolean tmpShowValidatedValuesInTraceLogging = configuration != null ? configuration.getShowValidatedValuesInTraceLogs() : false;

		String propertyStringValue = properties.get( HibernateValidatorConfiguration.SHOW_VALIDATED_VALUE_IN_TRACE_LOGS );
		if ( propertyStringValue != null ) {
			boolean configurationValue = Boolean.valueOf( propertyStringValue );
			// throw an exception if the programmatic value is true, and it overrides a false configured value
			if ( tmpShowValidatedValuesInTraceLogging && !configurationValue ) {
				throw LOG.getInconsistentShowValidatedValuesInTraceLogsViolationConfigurationException();
			}
			tmpShowValidatedValuesInTraceLogging = configurationValue;
		}

		return tmpShowValidatedValuesInTraceLogging;
	}

	static void logValidatorFactoryScopedConfiguration(ValidatorFactoryScopedContext context) {
		LOG.logValidatorFactoryScopedConfiguration( context.getMessageInterpolator().getClass(), "message interpolator" );
		LOG.logValidatorFactoryScopedConfiguration( context.getTraversableResolver().getClass(), "traversable resolver" );
		LOG.logValidatorFactoryScopedConfiguration( context.getParameterNameProvider().getClass(), "parameter name provider" );
		LOG.logValidatorFactoryScopedConfiguration( context.getClockProvider().getClass(), "clock provider" );
		LOG.logValidatorFactoryScopedConfiguration( context.getScriptEvaluatorFactory().getClass(), "script evaluator factory" );
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
