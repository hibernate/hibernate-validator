/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;

/**
 * Check that a character sequence is not {@code null} nor {@link String#isBlank() blank}.
 *
 * @author Guillaume Smet
 */
public class NotBlankValidator implements ConstraintValidator<NotBlank, CharSequence> {

	/**
	 * Checks that the character sequence is not {@code null} nor {@link String#isBlank() blank}.
	 *
	 * @param charSequence the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the string is not {@code null} and
	 * the call to {@link String#isBlank() charSequence.isBlank()} returns {@code false}, {@code false} otherwise
	 * @see String#isBlank()
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return false;
		}

		return !charSequence.toString().isBlank();
	}
}
