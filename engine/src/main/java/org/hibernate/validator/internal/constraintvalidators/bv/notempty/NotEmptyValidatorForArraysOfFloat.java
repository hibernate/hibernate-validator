/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.notempty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;

/**
 * @author Guillaume Smet
*/
public class NotEmptyValidatorForArraysOfFloat implements ConstraintValidator<NotEmpty, float[]> {

	/**
	 * Checks the array is not {@code null} and not empty.
	 *
	 * @param array the array to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the array is not {@code null} and the array is not empty
	 */
	@Override
	public boolean isValid(float[] array, ConstraintValidatorContext constraintValidatorContext) {
		if ( array == null ) {
			return false;
		}
		return array.length > 0;
	}
}
