/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.UpperCase;

/**
 * Checks that the character sequence contains only uppercase letters.
 * Non-letter characters are ignored.
 *
 * @author Andrea Boriero
 */
public class UpperCaseValidator implements ConstraintValidator<UpperCase, CharSequence> {

	@Override
	public boolean isValid(
			CharSequence value,
			ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// All characters must either be non-letters or uppercase letters
		for ( int i = 0; i < value.length(); i++ ) {
			char ch = value.charAt( i );
			if ( Character.isLetter( ch ) && !Character.isUpperCase( ch ) ) {
				return false;
			}
		}
		return true;
	}
}
