/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Range;

/**
 * Shared bounds and null handling for range validators.
 */
public abstract class AbstractRangeValidator<T> implements ConstraintValidator<Range, T> {
	protected long minValue;
	protected long maxValue;

	@Override
	public void initialize(Range constraintAnnotation) {
		minValue = constraintAnnotation.min();
		maxValue = constraintAnnotation.max();
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		return value == null || isInRange( value );
	}

	protected abstract boolean isInRange(T value);
}
