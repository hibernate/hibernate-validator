/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.notempty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;

/**
 * @author Guillaume Smet
 */
public class NotEmptyValidatorForArraysOfChar implements ConstraintValidator<NotEmpty, char[]> {

	/**
	 * Checks the array is not {@code null} and not empty.
	 *
	 * @param array the array to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the array is not {@code null} and the array is not empty
	 */
	@Override
	public boolean isValid(char[] array, ConstraintValidatorContext constraintValidatorContext) {
		if ( array == null ) {
			return false;
		}
		return array.length > 0;
	}
}
