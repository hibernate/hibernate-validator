/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutil;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

public class PreconfiguredConstraintValidatorFactory implements ConstraintValidatorFactory {

	private final Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> defaultValidators;
	private final ConstraintValidatorFactory delegated;

	private PreconfiguredConstraintValidatorFactory(Builder builder) {
		this.defaultValidators = builder.defaultValidators;
		this.delegated = builder.delegated;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		if ( defaultValidators.containsKey( key ) ) {
			return (T) defaultValidators.get( key );
		}

		return delegated.getInstance( key );
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		delegated.releaseInstance( instance );
	}

	public static class Builder {

		private ConstraintValidatorFactory delegated;
		private final Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> defaultValidators = new HashMap<>();

		private Builder() {
		}

		public Builder defaultValidators(
				Map<Class<? extends ConstraintValidator>, ConstraintValidator<?, ?>> validators) {
			this.defaultValidators.putAll( validators );
			return this;
		}

		public Builder delegated(
				ConstraintValidatorFactory delegated) {
			this.delegated = delegated;
			return this;
		}

		public PreconfiguredConstraintValidatorFactory build() {
			return new PreconfiguredConstraintValidatorFactory( this );
		}
	}
}
