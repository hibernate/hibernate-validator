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

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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

	private ConstraintValidatorFactory constraintValidatorFactory;
	private final ValidatorFactoryScopedContext.Builder validatorFactoryScopedContextBuilder;
	private final ConstraintCreationContext constraintCreationContext;
	private final MethodValidationConfiguration.Builder methodValidationConfigurationBuilder;
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractorDescriptors;

	public ValidatorContextImpl(ValidatorFactoryImpl validatorFactory) {
		this.validatorFactoryScopedContextBuilder = new ValidatorFactoryScopedContext.Builder( validatorFactory.getValidatorFactoryScopedContext() );
		this.validatorFactory = validatorFactory;
		this.constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		this.constraintCreationContext = validatorFactory.getConstraintCreationContext();
		this.methodValidationConfigurationBuilder = new MethodValidationConfiguration.Builder( validatorFactory.getMethodValidationConfiguration() );
		this.valueExtractorDescriptors = new HashMap<>();
	}

	@Override
	public HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		validatorFactoryScopedContextBuilder.setMessageInterpolator( messageInterpolator );
		return this;
	}

	@Override
	public HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		validatorFactoryScopedContextBuilder.setTraversableResolver( traversableResolver );
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
		validatorFactoryScopedContextBuilder.setParameterNameProvider( parameterNameProvider );
		return this;
	}

	@Override
	public HibernateValidatorContext clockProvider(ClockProvider clockProvider) {
		validatorFactoryScopedContextBuilder.setClockProvider( clockProvider );
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
		validatorFactoryScopedContextBuilder.setFailFast( failFast );
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
		validatorFactoryScopedContextBuilder.setTraversableResolverResultCacheEnabled( enabled );
		return this;
	}

	@Override
	public HibernateValidatorContext temporalValidationTolerance(Duration temporalValidationTolerance) {
		validatorFactoryScopedContextBuilder.setTemporalValidationTolerance( temporalValidationTolerance );
		return this;
	}

	@Override
	public HibernateValidatorContext constraintValidatorPayload(Object dynamicPayload) {
		validatorFactoryScopedContextBuilder.setConstraintValidatorPayload( dynamicPayload );
		return this;
	}

	@Override
	public Validator getValidator() {
		return validatorFactory.createValidator(
				constraintValidatorFactory,
				valueExtractorDescriptors.isEmpty()
						? constraintCreationContext
						: new ConstraintCreationContext( constraintCreationContext.getConstraintHelper(),
								constraintCreationContext.getConstraintValidatorManager(), constraintCreationContext.getTypeResolutionHelper(),
								new ValueExtractorManager( constraintCreationContext.getValueExtractorManager(), valueExtractorDescriptors ) ),
				validatorFactoryScopedContextBuilder.build(),
				methodValidationConfigurationBuilder.build() );
	}
}
