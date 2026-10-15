/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Alphabetic;

/**
 * Checks that the character sequence contains only letters.
 *
 * @author Andrea Boriero
 */
public class AlphabeticValidator implements ConstraintValidator<Alphabetic, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// Every character must be a letter
		for ( int i = 0; i < value.length(); i++ ) {
			if ( !Character.isLetter( value.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}
}
