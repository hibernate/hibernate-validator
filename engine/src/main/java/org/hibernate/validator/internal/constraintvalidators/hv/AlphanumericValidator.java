/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Alphanumeric;

/**
 * Checks that the character sequence contains only letters and digits.
 *
 * @author Andrea Boriero
 */
public class AlphanumericValidator implements ConstraintValidator<Alphanumeric, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// Every character must be a letter or a digit
		for ( int i = 0; i < value.length(); i++ ) {
			if ( !Character.isLetterOrDigit( value.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}
}
