/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutil;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;

/**
 * This class provides useful functions to create {@code ValidatorFactory} with preconfigured validators to test Bean
 * validation without creation of custom validator instances.
 *
 * @author Attila Hajdu
 */
@SuppressWarnings("rawtypes")
public class PreconfiguredValidatorsValidatorFactory implements ValidatorFactory {
	private final Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> defaultValidators;
	private final ValidatorFactory delegated;

	private PreconfiguredValidatorsValidatorFactory(Builder builder) {
		this.defaultValidators = builder.defaultValidators;

		ValidatorFactory defaultValidationFactory = Validation.buildDefaultValidatorFactory();
		ConstraintValidatorFactory wrappedConstraintValidatorFactory = PreconfiguredConstraintValidatorFactory.builder()
				.delegated( defaultValidationFactory.getConstraintValidatorFactory() )
				.defaultValidators( this.defaultValidators ).build();

		this.delegated = Validation.byDefaultProvider().configure()
				.constraintValidatorFactory( wrappedConstraintValidatorFactory )
				.buildValidatorFactory();

	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Validator getValidator() {
		return delegated.getValidator();
	}

	@Override
	public ValidatorContext usingContext() {
		return delegated.usingContext();
	}

	@Override
	public MessageInterpolator getMessageInterpolator() {
		return delegated.getMessageInterpolator();
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return delegated.getTraversableResolver();
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return delegated.getConstraintValidatorFactory();
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return delegated.getParameterNameProvider();
	}

	@Override
	public ClockProvider getClockProvider() {
		return delegated.getClockProvider();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		return delegated.unwrap( type );
	}

	@Override
	public void close() {
		delegated.close();
	}

	public static class Builder {

		private Builder() {
		}

		private final Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> defaultValidators = new HashMap<>();

		public Builder defaultValidators(
				Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> defaultValidators) {
			this.defaultValidators.putAll( defaultValidators );
			return this;
		}

		public PreconfiguredValidatorsValidatorFactory build() {
			return new PreconfiguredValidatorsValidatorFactory( this );
		}
	}
}
