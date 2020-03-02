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
 * Check that the array is not null and not empty.
 *
 * @author Guillaume Smet
 */
public class NotEmptyValidatorForArray implements ConstraintValidator<NotEmpty, Object[]> {

	/**
	 * Checks the array is not {@code null} and not empty.
	 *
	 * @param array the array to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the array is not {@code null} and the array is not empty
	 */
	@Override
	public boolean isValid(Object[] array, ConstraintValidatorContext constraintValidatorContext) {
		if ( array == null ) {
			return false;
		}
		return array.length > 0;
	}
}
