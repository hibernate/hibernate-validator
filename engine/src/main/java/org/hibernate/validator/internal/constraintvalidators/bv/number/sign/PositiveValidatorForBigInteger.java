/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Positive;

/**
 * Check that the number being validated is positive.
 *
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class PositiveValidatorForBigInteger implements ConstraintValidator<Positive, BigInteger> {

	@Override
	public boolean isValid(BigInteger value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return NumberSignHelper.signum( value ) > 0;
	}
}
