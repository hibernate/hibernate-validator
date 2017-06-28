/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.size;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
*/
public class SizeValidatorForArraysOfLong extends SizeValidatorForArraysOfPrimitives
		implements ConstraintValidator<Size, long[]> {

	/**
	 * Checks the number of entries in an array.
	 *
	 * @param array The array to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns {@code true} if the array is {@code null} or the number of entries in
	 *         {@code array} is between the specified {@code min} and {@code max} values (inclusive),
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isValid(long[] array, ConstraintValidatorContext constraintValidatorContext) {
		if ( array == null ) {
			return true;
		}
		return array.length >= min && array.length <= max;
	}
}
