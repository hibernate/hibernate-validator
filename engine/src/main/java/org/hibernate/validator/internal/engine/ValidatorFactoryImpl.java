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
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineBeanMetaDataClassNormalizer;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintMappings;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintValidatorPayload;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineConstraintExpressionLanguageFeatureLevel;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.determineCustomViolationExpressionLanguageFeatureLevel;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManagerImpl;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManagerImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.XmlMetaDataProvider;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.internal.util.stereotypes.ThreadSafe;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
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
	 * Programmatic constraints passed via the Hibernate Validator specific API. Empty if there are
	 * no programmatic constraints
	 */
	@Immutable
	private final Set<DefaultConstraintMapping> constraintMappings;

	/**
	 * The constraint creation context containing all the helpers necessary to the constraint creation.
	 */
	private final ConstraintCreationContext constraintCreationContext;

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
	private final ConcurrentMap<BeanMetaDataManagerKey, BeanMetaDataManager> beanMetaDataManagers = new ConcurrentHashMap<>();

	private final JavaBeanHelper javaBeanHelper;

	private final BeanMetaDataClassNormalizer beanMetadataClassNormalizer;

	private final ValidationOrderGenerator validationOrderGenerator;

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		ClassLoader externalClassLoader = determineExternalClassLoader( configurationState );

		ConfigurationImpl hibernateSpecificConfig = null;
		if ( configurationState instanceof ConfigurationImpl ) {
			hibernateSpecificConfig = (ConfigurationImpl) configurationState;
		}

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

		ConstraintValidatorManager constraintValidatorManager = new ConstraintValidatorManagerImpl(
				configurationState.getConstraintValidatorFactory(),
				this.validatorFactoryScopedContext.getConstraintValidatorInitializationContext()
		);

		this.validationOrderGenerator = new ValidationOrderGenerator();

		ValueExtractorManager valueExtractorManager = new ValueExtractorManager( configurationState.getValueExtractors() );
		ConstraintHelper constraintHelper = ConstraintHelper.forAllBuiltinConstraints();
		TypeResolutionHelper typeResolutionHelper = new TypeResolutionHelper();

		this.constraintCreationContext = new ConstraintCreationContext( constraintHelper, constraintValidatorManager, typeResolutionHelper, valueExtractorManager );

		this.executableHelper = new ExecutableHelper( typeResolutionHelper );
		this.javaBeanHelper = new JavaBeanHelper( ValidatorFactoryConfigurationHelper.determineGetterPropertySelectionStrategy( hibernateSpecificConfig, properties, externalClassLoader ),
				ValidatorFactoryConfigurationHelper.determinePropertyNodeNameProvider( hibernateSpecificConfig, properties, externalClassLoader ) );
		this.beanMetadataClassNormalizer = determineBeanMetaDataClassNormalizer( hibernateSpecificConfig );

		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider( constraintCreationContext, javaBeanHelper, configurationState.getMappingStreams(), externalClassLoader );
		}

		this.constraintMappings = Collections.unmodifiableSet(
				determineConstraintMappings(
						typeResolutionHelper,
						configurationState,
						javaBeanHelper,
						externalClassLoader
				)
		);

		registerCustomConstraintValidators( constraintMappings, constraintHelper );

		if ( LOG.isDebugEnabled() ) {
			logValidatorFactoryScopedConfiguration( validatorFactoryScopedContext );
		}
	}

	@Override
	public Validator getValidator() {
		return createValidator(
				constraintCreationContext.getConstraintValidatorManager().getDefaultConstraintValidatorFactory(),
				constraintCreationContext,
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
		return constraintCreationContext.getConstraintValidatorManager().getDefaultConstraintValidatorFactory();
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
		return javaBeanHelper.getGetterPropertySelectionStrategy();
	}

	@Override
	public PropertyNodeNameProvider getPropertyNodeNameProvider() {
		return javaBeanHelper.getPropertyNodeNameProvider();
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

	ConstraintCreationContext getConstraintCreationContext() {
		return constraintCreationContext;
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
		constraintCreationContext.getConstraintValidatorManager().clear();
		constraintCreationContext.getConstraintHelper().clear();
		for ( BeanMetaDataManager beanMetaDataManager : beanMetaDataManagers.values() ) {
			beanMetaDataManager.clear();
		}
		validatorFactoryScopedContext.getScriptEvaluatorFactory().clear();
		constraintCreationContext.getValueExtractorManager().clear();
	}

	public ValidatorFactoryScopedContext getValidatorFactoryScopedContext() {
		return this.validatorFactoryScopedContext;
	}

	Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory,
			ConstraintCreationContext constraintCreationContext,
			ValidatorFactoryScopedContext validatorFactoryScopedContext,
			MethodValidationConfiguration methodValidationConfiguration) {
		BeanMetaDataManager beanMetaDataManager = beanMetaDataManagers.computeIfAbsent(
				new BeanMetaDataManagerKey( validatorFactoryScopedContext.getParameterNameProvider(), constraintCreationContext.getValueExtractorManager(), methodValidationConfiguration ),
				key -> new BeanMetaDataManagerImpl(
						constraintCreationContext,
						executableHelper,
						validatorFactoryScopedContext.getParameterNameProvider(),
						javaBeanHelper,
						beanMetadataClassNormalizer,
						validationOrderGenerator,
						buildMetaDataProviders(),
						methodValidationConfiguration
				)
		);

		return new ValidatorImpl(
				constraintValidatorFactory,
				beanMetaDataManager,
				constraintCreationContext.getValueExtractorManager(),
				constraintCreationContext.getConstraintValidatorManager(),
				validationOrderGenerator,
				validatorFactoryScopedContext
		);
	}

	private List<MetaDataProvider> buildMetaDataProviders() {
		List<MetaDataProvider> metaDataProviders = newArrayList();
		if ( xmlMetaDataProvider != null ) {
			metaDataProviders.add( xmlMetaDataProvider );
		}

		if ( !constraintMappings.isEmpty() ) {
			metaDataProviders.add(
					new ProgrammaticMetaDataProvider(
							constraintCreationContext,
							constraintMappings
					)
			);
		}
		return metaDataProviders;
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
