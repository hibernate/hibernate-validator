/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Max;

/**
 * Check that the monetary amount being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class MaxValidatorForMonetaryAmount implements ConstraintValidator<Max, MonetaryAmount> {

	private BigDecimal maxValue;

	@Override
	public void initialize(Max maxValue) {
		this.maxValue = BigDecimal.valueOf( maxValue.value() );
	}

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return value.getNumber().numberValueExact( BigDecimal.class ).compareTo( maxValue ) <= 0;
	}

}
