/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Range;

/**
 * Validates both inclusive bounds using a single decimal conversion.
 */
public class RangeValidatorForMonetaryAmount implements ConstraintValidator<Range, MonetaryAmount> {

	private BigDecimal min;
	private BigDecimal max;

	@Override
	public void initialize(Range constraintAnnotation) {
		min = BigDecimal.valueOf( constraintAnnotation.min() );
		max = BigDecimal.valueOf( constraintAnnotation.max() );
	}

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		return value == null || isInRange( value );
	}

	protected boolean isInRange(MonetaryAmount value) {
		BigDecimal number = value.getNumber().numberValueExact( BigDecimal.class );
		return number.compareTo( min ) >= 0 && number.compareTo( max ) <= 0;
	}
}
