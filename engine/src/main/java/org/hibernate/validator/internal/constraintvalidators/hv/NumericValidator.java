/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Numeric;

/**Hi Rebecca, I’m really sorry, but I think my fever has gone up, and I’m struggling to stay in front of the screen right now. I don’t think I’ll be able to make our meeting. I’m really sorry for the short notice,
 * Checks that the character sequence contains only digits.
 *
 * @author Andrea Boriero
 */
public class NumericValidator implements ConstraintValidator<Numeric, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// Every character must be a digit
		for ( int i = 0; i < value.length(); i++ ) {
			if ( !Character.isDigit( value.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}
}
