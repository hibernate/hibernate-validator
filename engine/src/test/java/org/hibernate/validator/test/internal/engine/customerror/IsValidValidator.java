/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.customerror;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class IsValidValidator implements ConstraintValidator<IsValid, DummyTestClass> {

	public static final String message = "Custom error message";

	@Override
	public boolean isValid(DummyTestClass dummyTestClass, ConstraintValidatorContext constraintValidatorContext) {
		constraintValidatorContext.disableDefaultConstraintViolation();
		constraintValidatorContext.buildConstraintViolationWithTemplate( message )
				.addConstraintViolation();
		return false;
	}
}
