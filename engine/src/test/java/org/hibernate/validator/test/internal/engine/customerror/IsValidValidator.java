/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.customerror;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class IsValidValidator implements ConstraintValidator<IsValid, DummyTestClass> {

	public static final String message = "Custom error message";


	@Override
	public void initialize(IsValid isValid) {
	}

	@Override
	public boolean isValid(DummyTestClass dummyTestClass, ConstraintValidatorContext constraintValidatorContext) {
		constraintValidatorContext.disableDefaultConstraintViolation();
		constraintValidatorContext.buildConstraintViolationWithTemplate( message )
				.addConstraintViolation();
		return false;
	}
}
