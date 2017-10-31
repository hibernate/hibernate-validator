/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 */
public class ValidatorContextImpl implements HibernateValidatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ValidatorFactoryImpl validatorFactory;

	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private ConstraintValidatorFactory constraintValidatorFactory;
	private ExecutableParameterNameProvider parameterNameProvider;
	private ClockProvider clockProvider;
	private ScriptEvaluatorFactory scriptEvaluatorFactory;
	private Duration temporalValidationTolerance;
	private boolean failFast;
	private boolean traversableResolverResultCacheEnabled;
	private final ValueExtractorManager valueExtractorManager;
	private final MethodValidationConfiguration.Builder methodValidationConfigurationBuilder;
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractorDescriptors;

	public ValidatorContextImpl(ValidatorFactoryImpl validatorFactory) {
		this.validatorFactory = validatorFactory;
		this.messageInterpolator = validatorFactory.getMessageInterpolator();
		this.traversableResolver = validatorFactory.getTraversableResolver();
		this.constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		this.parameterNameProvider = validatorFactory.getExecutableParameterNameProvider();
		this.clockProvider = validatorFactory.getClockProvider();
		this.scriptEvaluatorFactory = validatorFactory.getScriptEvaluatorFactory();
		this.temporalValidationTolerance = validatorFactory.getTemporalValidationTolerance();
		this.failFast = validatorFactory.isFailFast();
		this.traversableResolverResultCacheEnabled = validatorFactory.isTraversableResolverResultCacheEnabled();
		this.methodValidationConfigurationBuilder = new MethodValidationConfiguration.Builder( validatorFactory.getMethodValidationConfiguration() );
		this.valueExtractorManager = validatorFactory.getValueExtractorManager();
		this.valueExtractorDescriptors = new HashMap<>();
	}

	@Override
	public HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		if ( messageInterpolator == null ) {
			this.messageInterpolator = validatorFactory.getMessageInterpolator();
		}
		else {
			this.messageInterpolator = messageInterpolator;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		if ( traversableResolver == null ) {
			this.traversableResolver = validatorFactory.getTraversableResolver();
		}
		else {
			this.traversableResolver = traversableResolver;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
		if ( factory == null ) {
			this.constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		}
		else {
			this.constraintValidatorFactory = factory;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		if ( parameterNameProvider == null ) {
			this.parameterNameProvider = validatorFactory.getExecutableParameterNameProvider();
		}
		else {
			this.parameterNameProvider = new ExecutableParameterNameProvider( parameterNameProvider );
		}
		return this;
	}

	@Override
	public HibernateValidatorContext clockProvider(ClockProvider clockProvider) {
		if ( clockProvider == null ) {
			this.clockProvider = validatorFactory.getClockProvider();
		}
		else {
			this.clockProvider = clockProvider;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext addValueExtractor(ValueExtractor<?> extractor) {
		ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( extractor );
		ValueExtractorDescriptor previous = valueExtractorDescriptors.put( descriptor.getKey(), descriptor );

		if ( previous != null ) {
			throw LOG.getValueExtractorForTypeAndTypeUseAlreadyPresentException( extractor, previous.getValueExtractor() );
		}

		return this;
	}

	@Override
	public HibernateValidatorContext failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	@Override
	public HibernateValidatorContext allowOverridingMethodAlterParameterConstraint(boolean allow) {
		methodValidationConfigurationBuilder.allowOverridingMethodAlterParameterConstraint( allow );
		return this;
	}

	@Override
	public HibernateValidatorContext allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		methodValidationConfigurationBuilder.allowMultipleCascadedValidationOnReturnValues( allow );
		return this;
	}

	@Override
	public HibernateValidatorContext allowParallelMethodsDefineParameterConstraints(boolean allow) {
		methodValidationConfigurationBuilder.allowParallelMethodsDefineParameterConstraints( allow );
		return this;
	}

	@Override
	public HibernateValidatorContext enableTraversableResolverResultCache(boolean enabled) {
		this.traversableResolverResultCacheEnabled = enabled;
		return this;
	}

	@Override
	public HibernateValidatorContext temporalValidationTolerance(Duration temporalValidationTolerance) {
		this.temporalValidationTolerance = temporalValidationTolerance == null ? Duration.ZERO : temporalValidationTolerance.abs();
		return this;
	}

	@Override
	public Validator getValidator() {
		return validatorFactory.createValidator(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				clockProvider,
				scriptEvaluatorFactory,
				failFast,
				temporalValidationTolerance,
				valueExtractorDescriptors.isEmpty() ? valueExtractorManager : new ValueExtractorManager( valueExtractorManager, valueExtractorDescriptors ),
				methodValidationConfigurationBuilder.build(),
				traversableResolverResultCacheEnabled
		);
	}
}
