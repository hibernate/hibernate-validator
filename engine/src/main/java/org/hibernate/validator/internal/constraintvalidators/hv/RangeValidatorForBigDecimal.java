/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Range;

/**
 * Validates both inclusive bounds without losing precision.
 */
public class RangeValidatorForBigDecimal implements ConstraintValidator<Range, BigDecimal> {
	private BigDecimal min;
	private BigDecimal max;

	@Override
	public void initialize(Range constraintAnnotation) {
		min = BigDecimal.valueOf( constraintAnnotation.min() );
		max = BigDecimal.valueOf( constraintAnnotation.max() );
	}

	@Override
	public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
		return value == null || isInRange( value );
	}

	private boolean isInRange(BigDecimal value) {
		return value.compareTo( min ) >= 0 && value.compareTo( max ) <= 0;
	}
}
