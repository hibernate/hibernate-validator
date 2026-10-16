/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Range;

/**
 * Validates both inclusive bounds without losing precision.
 */
public class RangeValidatorForBigInteger implements ConstraintValidator<Range, BigInteger> {
	private BigInteger min;
	private BigInteger max;

	@Override
	public void initialize(Range constraintAnnotation) {
		min = BigInteger.valueOf( constraintAnnotation.min() );
		max = BigInteger.valueOf( constraintAnnotation.max() );
	}

	@Override
	public boolean isValid(BigInteger value, ConstraintValidatorContext context) {
		return value == null || isInRange( value );
	}

	protected boolean isInRange(BigInteger value) {
		return value.compareTo( min ) >= 0 && value.compareTo( max ) <= 0;
	}
}
