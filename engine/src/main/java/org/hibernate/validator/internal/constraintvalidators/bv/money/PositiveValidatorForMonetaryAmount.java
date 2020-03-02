/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Positive;

/**
 * Check that the number being validated positive.
 *
 * @author Marko Bekhta
 */
public class PositiveValidatorForMonetaryAmount implements ConstraintValidator<Positive, MonetaryAmount> {

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return value.signum() > 0;
	}
}
