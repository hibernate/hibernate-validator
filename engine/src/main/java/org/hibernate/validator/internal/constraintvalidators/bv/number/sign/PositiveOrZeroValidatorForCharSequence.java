/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Check that the number being validated is positive or zero.
 *
 * @author Guillaume Smet
 */
public class PositiveOrZeroValidatorForCharSequence implements ConstraintValidator<PositiveOrZero, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		try {
			return NumberSignHelper.signum( new BigDecimal( value.toString() ) ) >= 0;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
