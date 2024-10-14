/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class SuperTypeArrayValidator implements ConstraintValidator<SuperTypeArray, SuperType[]> {

	@Override
	public boolean isValid(SuperType[] value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
