/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.sign;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NegativeOrZero;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * Check that the number being validated is negative.
 *
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class NegativeOrZeroValidatorForDouble implements ConstraintValidator<NegativeOrZero, Double> {

	@Override
	public boolean isValid(Double value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return NumberSignHelper.signum( value, InfinityNumberComparatorHelper.GREATER_THAN ) <= 0;
	}
}
