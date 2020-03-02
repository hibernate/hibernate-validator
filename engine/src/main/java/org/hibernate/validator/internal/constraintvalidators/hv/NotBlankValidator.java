/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Check that a character sequence's (e.g. string) trimmed length is not empty.
 *
 * @author Hardy Ferentschik
 */
@SuppressWarnings("deprecation")
public class NotBlankValidator implements ConstraintValidator<NotBlank, CharSequence> {

	/**
	 * Checks that the trimmed string is not empty.
	 *
	 * @param charSequence the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the string is {@code null} or the length of the trimmed
	 * {@code charSequence} is strictly superior to 0, {@code false} otherwise
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return true;
		}

		return charSequence.toString().trim().length() > 0;
	}
}
