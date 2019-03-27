/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.time.Duration;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidatorContext;

/**
 * @author Guillaume Smet
 */
public class PredefinedScopeValidatorContextImpl implements HibernateValidatorContext {

	private final PredefinedScopeValidatorFactoryImpl validatorFactory;

	private final ValidatorFactoryScopedContext.Builder validatorFactoryScopedContextBuilder;

	public PredefinedScopeValidatorContextImpl(PredefinedScopeValidatorFactoryImpl validatorFactory) {
		this.validatorFactoryScopedContextBuilder = new ValidatorFactoryScopedContext.Builder( validatorFactory.getValidatorFactoryScopedContext() );
		this.validatorFactory = validatorFactory;
	}

	@Override
	public HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		throw new IllegalStateException( "Defining a Validator-specific message interpolator is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		throw new IllegalStateException( "Defining a Validator-specific traversable resolver is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
		throw new IllegalStateException( "Defining a Validator-specific constraint validator factory is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		throw new IllegalStateException( "Defining a Validator-specific parameter name provider is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext clockProvider(ClockProvider clockProvider) {
		throw new IllegalStateException( "Defining a Validator-specific clock provider is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext addValueExtractor(ValueExtractor<?> extractor) {
		throw new IllegalStateException( "Adding Validator-specific value extractors is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext failFast(boolean failFast) {
		validatorFactoryScopedContextBuilder.setFailFast( failFast );
		return this;
	}

	@Override
	public HibernateValidatorContext allowOverridingMethodAlterParameterConstraint(boolean allow) {
		throw new IllegalStateException( "Altering method validation configuration is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		throw new IllegalStateException( "Altering method validation configuration is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext allowParallelMethodsDefineParameterConstraints(boolean allow) {
		throw new IllegalStateException( "Altering method validation configuration is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext enableTraversableResolverResultCache(boolean enabled) {
		validatorFactoryScopedContextBuilder.setTraversableResolverResultCacheEnabled( enabled );
		return this;
	}

	@Override
	public HibernateValidatorContext temporalValidationTolerance(Duration temporalValidationTolerance) {
		throw new IllegalStateException( "Defining a Validator-specific temporal validation tolerance is not supported by the predefined scope ValidatorFactory." );
	}

	@Override
	public HibernateValidatorContext constraintValidatorPayload(Object dynamicPayload) {
		validatorFactoryScopedContextBuilder.setConstraintValidatorPayload( dynamicPayload );
		return this;
	}

	@Override
	public Validator getValidator() {
		return validatorFactory.createValidator( validatorFactoryScopedContextBuilder.build() );
	}
}
