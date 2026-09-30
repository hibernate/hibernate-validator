/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.LowerCase;

/**
 * Checks that the character sequence contains only lowercase letters.
 * Non-letter characters are ignored.
 *
 * @author Andrea Boriero
 */
public class LowerCaseValidator implements ConstraintValidator<LowerCase, CharSequence> {

	@Override
	public boolean isValid(
			CharSequence value,
			ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// All characters must either be non-letters or lowercase letters
		return value.chars().allMatch( ch -> !Character.isLetter( ch ) || Character.isLowerCase( ch ) );
	}
}
