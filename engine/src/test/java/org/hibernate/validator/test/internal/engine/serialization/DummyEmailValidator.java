/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.serialization;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class DummyEmailValidator implements ConstraintValidator<Email, String> {
	public void initialize(Email annotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		return false;
	}
}


