/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.notempty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;

/**
 * Check that the character sequence is not null and its length is strictly superior to 0.
 *
 * @author Guillaume Smet
 */
public class NotEmptyValidatorForCharSequence implements ConstraintValidator<NotEmpty, CharSequence> {

	/**
	 * Checks the character sequence is not {@code null} and not empty.
	 *
	 * @param charSequence the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the character sequence is not {@code null} and not empty.
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return false;
		}
		return charSequence.length() > 0;
	}
}
