/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Negative;

/**
 * Check that the number being validated is negative.
 *
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class NegativeValidatorForLong implements ConstraintValidator<Negative, Long> {

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return NumberSignHelper.signum( value ) < 0;
	}
}
