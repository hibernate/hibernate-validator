/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NullOrNotBlank;

/**
 * Check that the character sequence is either {@code null} or not {@link String#isBlank() blank}.
 *
 * @author Koen Aers
 */
public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, CharSequence> {

	/**
	 * Checks that the character sequence is {@code null} or not {@link String#isBlank() blank}.
	 *
	 * @param charSequence the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the string is {@code null} or
	 * the call to {@link String#isBlank() charSequence.isBlank()} returns {@code false}, {@code false} otherwise
	 * @see String#isBlank()
	 */
	@Override
	public boolean isValid(
			CharSequence charSequence,
			ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return true;
		}
		else {
			return !charSequence.toString().isBlank();
		}
	}
}
