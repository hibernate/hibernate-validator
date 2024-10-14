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
public class SerializableConstraintValidator implements ConstraintValidator<Serializable, java.io.Serializable> {

	@Override
	public boolean isValid(java.io.Serializable value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
