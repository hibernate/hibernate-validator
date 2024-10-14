/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Null;

/**
 * Validate that the object is {@code null}
 *
 * @author Alaa Nassef
 */
public class NullValidator implements ConstraintValidator<Null, Object> {

	@Override
	public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
		return object == null;
	}

}
