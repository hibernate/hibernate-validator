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
public class ObjectConstraintValidator implements ConstraintValidator<org.hibernate.validator.test.constraints.Object, java.lang.Object> {

	@Override
	public boolean isValid(java.lang.Object value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
