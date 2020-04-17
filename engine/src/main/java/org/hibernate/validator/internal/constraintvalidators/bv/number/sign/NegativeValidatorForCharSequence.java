/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Negative;

/**
 * Check that the number being validated is negative.
 *
 * @author Guillaume Smet
 */
public class NegativeValidatorForCharSequence implements ConstraintValidator<Negative, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		try {
			return NumberSignHelper.signum( new BigDecimal( value.toString() ) ) < 0;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
