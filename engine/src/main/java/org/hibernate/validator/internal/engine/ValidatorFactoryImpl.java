/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getAllowMultipleCascadedValidationOnReturnValues;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getAllowOverridingMethodAlterParameterConstraint;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getAllowParallelMethodsDefineParameterConstraints;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getConstraintMappings;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getConstraintValidatorPayload;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getExternalClassLoader;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getFailFast;
import static org.hibernate.validator.internal.engine.ValidatorFactoryConfigurationHelper.getTraversableResolverResultCacheEnabled;
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

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;

import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
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

	private final JavaBeanHelper javaBeanHelper;

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

		Map<String, String> properties = configurationState.getProperties();

		this.javaBeanHelper = new JavaBeanHelper( ValidatorFactoryConfigurationHelper.getGetterPropertySelectionStrategy( hibernateSpecificConfig, properties, externalClassLoader ) );

		// HV-302; don't load XmlMappingParser if not necessary
		if ( configurationState.getMappingStreams().isEmpty() ) {
			this.xmlMetaDataProvider = null;
		}
		else {
			this.xmlMetaDataProvider = new XmlMetaDataProvider(
					constraintHelper, typeResolutionHelper, valueExtractorManager, javaBeanHelper, configurationState.getMappingStreams(), externalClassLoader
			);
		}

		this.constraintMappings = Collections.unmodifiableSet(
				getConstraintMappings(
						typeResolutionHelper,
						configurationState,
						javaBeanHelper,
						externalClassLoader
				)
		);

		registerCustomConstraintValidators( constraintMappings, constraintHelper );

		this.methodValidationConfiguration = new MethodValidationConfiguration.Builder()
				.allowOverridingMethodAlterParameterConstraint(
						getAllowOverridingMethodAlterParameterConstraint( hibernateSpecificConfig, properties )
				).allowMultipleCascadedValidationOnReturnValues(
						getAllowMultipleCascadedValidationOnReturnValues( hibernateSpecificConfig, properties )
				).allowParallelMethodsDefineParameterConstraints(
						getAllowParallelMethodsDefineParameterConstraints( hibernateSpecificConfig, properties )
				).build();

		this.validatorFactoryScopedContext = new ValidatorFactoryScopedContext(
				configurationState.getMessageInterpolator(),
				configurationState.getTraversableResolver(),
				new ExecutableParameterNameProvider( configurationState.getParameterNameProvider() ),
				configurationState.getClockProvider(),
				ValidatorFactoryConfigurationHelper.getTemporalValidationTolerance( configurationState, properties ),
				ValidatorFactoryConfigurationHelper.getScriptEvaluatorFactory( configurationState, properties, externalClassLoader ),
				getFailFast( hibernateSpecificConfig, properties ),
				getTraversableResolverResultCacheEnabled( hibernateSpecificConfig, properties ),
				getConstraintValidatorPayload( hibernateSpecificConfig )
		);

		this.constraintValidatorManager = new ConstraintValidatorManager(
				configurationState.getConstraintValidatorFactory(),
				this.validatorFactoryScopedContext.getConstraintValidatorInitializationContext()
		);

		this.validationOrderGenerator = new ValidationOrderGenerator();

		if ( LOG.isDebugEnabled() ) {
			logValidatorFactoryScopedConfiguration( validatorFactoryScopedContext );
		}
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

	@Override
	public GetterPropertySelectionStrategy getGetterPropertySelectionStrategy() {
		return javaBeanHelper.getGetterPropertySelectionStrategy();
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
						javaBeanHelper,
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
