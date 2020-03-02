/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.size;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the length of a character sequence is between min and max.
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 * @author Hardy Ferentschik
 */
public class SizeValidatorForCharSequence implements ConstraintValidator<Size, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int min;
	private int max;

	@Override
	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the length of the specified character sequence (e.g. string).
	 *
	 * @param charSequence The character sequence to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns {@code true} if the string is {@code null} or the length of {@code charSequence} between the specified
	 *         {@code min} and {@code max} values (inclusive), {@code false} otherwise.
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		if ( charSequence == null ) {
			return true;
		}
		int length = charSequence.length();
		return length >= min && length <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw LOG.getMinCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw LOG.getLengthCannotBeNegativeException();
		}
	}
}
