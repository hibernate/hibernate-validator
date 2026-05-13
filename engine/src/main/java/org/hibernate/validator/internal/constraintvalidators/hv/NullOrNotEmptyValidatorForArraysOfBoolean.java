/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NullOrNotEmpty;

public class NullOrNotEmptyValidatorForArraysOfBoolean implements ConstraintValidator<NullOrNotEmpty, boolean[]> {

	@Override
	public boolean isValid(boolean[] array, ConstraintValidatorContext constraintValidatorContext) {
		if ( array == null ) {
			return true;
		}
		return array.length > 0;
	}
}
