/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Min;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class MinValidatorForMonetaryAmount implements ConstraintValidator<Min, MonetaryAmount> {

	private BigDecimal minValue;

	@Override
	public void initialize(Min minValue) {
		this.minValue = BigDecimal.valueOf( minValue.value() );
	}

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return value.getNumber().numberValueExact( BigDecimal.class ).compareTo( minValue ) >= 0;
	}

}
