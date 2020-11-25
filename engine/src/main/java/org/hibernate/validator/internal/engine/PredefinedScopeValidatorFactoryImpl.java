/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineAllowMultipleCascadedValidationOnReturnValues;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineAllowOverridingMethodAlterParameterConstraint;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineAllowParallelMethodsDefineParameterConstraints;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintExpressionLanguageFeatureLevel;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineCustomViolationExpressionLanguageFeatureLevel;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineBeanMetaDataClassNormalizer;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintMappings;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintValidatorPayload;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineExternalClassLoader;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineFailFast;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineScriptEvaluatorFactory;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineTemporalValidationTolerance;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineTraversableResolverResultCacheEnabled;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.logValidatorFactoryScopedConfiguration;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.registerCustomConstraintValidators;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.PredefinedScopeHibernateValidatorFactory;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.PredefinedScopeConstraintValidatorManagerImpl;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.PredefinedScopeBeanMetaDataManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Factory returning initialized {@code Validator} instances.
 * <p>
 * This factory is designed to support a predefined scope of bean classes to validate and constraint validators.
 */
public class PredefinedScopeValidatorFactoryImpl implements PredefinedScopeHibernateValidatorFactory {

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
	 * Hibernate Validator specific flags to relax constraints on parameters.
	 */
	private final MethodValidationConfiguration methodValidationConfiguration;

	private final PredefinedScopeBeanMetaDataManager beanMetaDataManager;

	private final ValueExtractorManager valueExtractorManager;

	private final GetterPropertySelectionStrategy getterPropertySelectionStrategy;

	private final ValidationOrderGenerator validationOrderGenerator;

	public PredefinedScopeValidatorFactoryImpl(ConfigurationState configurationState) {
		Contracts.assertTrue( configurationState instanceof PredefinedScopeConfigurationImpl, "Only PredefinedScopeConfigurationImpl is supported." );

		ClassLoader externalClassLoader = determineExternalClassLoader( configurationState );

		PredefinedScopeConfigurationImpl hibernateSpecificConfig = (PredefinedScopeConfigurationImpl) configurationState;

		Map<String, String> properties = configurationState.getProperties();

		this.methodValidationConfiguration = new MethodValidationConfiguration.Builder()
				.allowOverridingMethodAlterParameterConstraint(
						determineAllowOverridingMethodAlterParameterConstraint( hibernateSpecificConfig, properties )
				).allowMultipleCascadedValidationOnReturnValues(
						determineAllowMultipleCascadedValidationOnReturnValues( hibernateSpecificConfig, properties )
				).allowParallelMethodsDefineParameterConstraints(
						determineAllowParallelMethodsDefineParameterConstraints( hibernateSpecificConfig, properties )
				).build();

		this.validatorFactoryScopedContext = new ValidatorFactoryScopedContext(
				configurationState.getMessageInterpolator(),
				configurationState.getTraversableResolver(),
				new ExecutableParameterNameProvider( configurationState.getParameterNameProvider() ),
				configurationState.getClockProvider(),
				determineTemporalValidationTolerance( configurationState, properties ),
				determineScriptEvaluatorFactory( configurationState, properties, externalClassLoader ),
				determineFailFast( hibernateSpecificConfig, properties ),
				determineTraversableResolverResultCacheEnabled( hibernateSpecificConfig, properties ),
				determineConstraintValidatorPayload( hibernateSpecificConfig ),
				determineConstraintExpressionLanguageFeatureLevel( hibernateSpecificConfig, properties ),
				determineCustomViolationExpressionLanguageFeatureLevel( hibernateSpecificConfig, properties )
		);

		this.constraintValidatorManager = new PredefinedScopeConstraintValidatorManagerImpl(
				configurationState.getConstraintValidatorFactory(),
				this.validatorFactoryScopedContext.getConstraintValidatorInitializationContext()
		);

		this.validationOrderGenerator = new ValidationOrderGenerator();

		this.getterPropertySelectionStrategy = ValidatorFactoryConfigurationHelper.determineGetterPropertySelectionStrategy( hibernateSpecificConfig, properties, externalClassLoader );

		this.valueExtractorManager = new ValueExtractorManager( configurationState.getValueExtractors() );
		ConstraintHelper constraintHelper = ConstraintHelper.forBuiltinConstraints( hibernateSpecificConfig.getBuiltinConstraints() );
		TypeResolutionHelper typeResolutionHelper = new TypeResolutionHelper();

		ConstraintCreationContext constraintCreationContext = new ConstraintCreationContext( constraintHelper,
				constraintValidatorManager, typeResolutionHelper, valueExtractorManager );

		ExecutableHelper executableHelper = new ExecutableHelper( typeResolutionHelper );
		JavaBeanHelper javaBeanHelper = new JavaBeanHelper( ValidatorFactoryConfigurationHelper.determineGetterPropertySelectionStrategy( hibernateSpecificConfig, properties, externalClassLoader ),
				ValidatorFactoryConfigurationHelper.determinePropertyNodeNameProvider( hibernateSpecificConfig, properties, externalClassLoader ) );

		// HV-302; don't load XmlMappingParser if not necessary
		XmlMetaDataProvider xmlMetaDataProvider;
		if ( configurationState.getMappingStreams().isEmpty() ) {
			xmlMetaDataProvider = null;
		}
		else {
			xmlMetaDataProvider = new XmlMetaDataProvider(
					constraintCreationContext, javaBeanHelper, configurationState.getMappingStreams(), externalClassLoader
			);
		}

		Set<DefaultConstraintMapping> constraintMappings = Collections.unmodifiableSet(
				determineConstraintMappings(
						typeResolutionHelper,
						configurationState,
						javaBeanHelper,
						externalClassLoader
				)
		);

		registerCustomConstraintValidators( constraintMappings, constraintHelper );

		this.beanMetaDataManager = new PredefinedScopeBeanMetaDataManager(
				constraintCreationContext,
				executableHelper,
				validatorFactoryScopedContext.getParameterNameProvider(),
				javaBeanHelper,
				validationOrderGenerator,
				buildMetaDataProviders( constraintCreationContext, xmlMetaDataProvider, constraintMappings ),
				methodValidationConfiguration,
				determineBeanMetaDataClassNormalizer( hibernateSpecificConfig ),
				hibernateSpecificConfig.getBeanClassesToInitialize()
		);

		if ( LOG.isDebugEnabled() ) {
			logValidatorFactoryScopedConfiguration( validatorFactoryScopedContext );
		}
	}

	@Override
	public Validator getValidator() {
		return createValidator( validatorFactoryScopedContext );
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

	@Override
	public GetterPropertySelectionStrategy getGetterPropertySelectionStrategy() {
		return getterPropertySelectionStrategy;
	}

	public boolean isFailFast() {
		return validatorFactoryScopedContext.isFailFast();
	}

	public boolean isTraversableResolverResultCacheEnabled() {
		return validatorFactoryScopedContext.isTraversableResolverResultCacheEnabled();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		// allow unwrapping into public super types
		if ( type.isAssignableFrom( PredefinedScopeHibernateValidatorFactory.class )
				|| type.isAssignableFrom( HibernateValidatorFactory.class ) ) {
			return type.cast( this );
		}
		throw LOG.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public HibernateValidatorContext usingContext() {
		return new PredefinedScopeValidatorContextImpl( this );
	}

	@Override
	public void close() {
		constraintValidatorManager.clear();
		beanMetaDataManager.clear();
		validatorFactoryScopedContext.getScriptEvaluatorFactory().clear();
		valueExtractorManager.clear();
	}

	public ValidatorFactoryScopedContext getValidatorFactoryScopedContext() {
		return this.validatorFactoryScopedContext;
	}

	Validator createValidator(ValidatorFactoryScopedContext validatorFactoryScopedContext) {
		return new ValidatorImpl(
				constraintValidatorManager.getDefaultConstraintValidatorFactory(),
				beanMetaDataManager,
				valueExtractorManager,
				constraintValidatorManager,
				validationOrderGenerator,
				validatorFactoryScopedContext
		);
	}

	private static List<MetaDataProvider> buildMetaDataProviders(
			ConstraintCreationContext constraintCreationContext,
			XmlMetaDataProvider xmlMetaDataProvider,
			Set<DefaultConstraintMapping> constraintMappings) {
		List<MetaDataProvider> metaDataProviders = newArrayList();
		if ( xmlMetaDataProvider != null ) {
			metaDataProviders.add( xmlMetaDataProvider );
		}

		if ( !constraintMappings.isEmpty() ) {
			metaDataProviders.add( new ProgrammaticMetaDataProvider( constraintCreationContext, constraintMappings ) );
		}
		return metaDataProviders;
	}
}
