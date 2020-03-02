/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;

/**
 * Check that a character sequence is not {@code null} nor empty after removing any leading or trailing whitespace.
 *
 * @author Guillaume Smet
 */
public class NotBlankValidator implements ConstraintValidator<NotBlank, CharSequence> {

	/**
	 * Checks that the character sequence is not {@code null} nor empty after removing any leading or trailing
	 * whitespace.
	 *
	 * @param charSequence the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the string is not {@code null} and the length of the trimmed
	 * {@code charSequence} is strictly superior to 0, {@code false} otherwise
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return false;
		}

		return charSequence.toString().trim().length() > 0;
	}
}
