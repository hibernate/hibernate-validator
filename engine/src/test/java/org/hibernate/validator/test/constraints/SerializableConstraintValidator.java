/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class SerializableConstraintValidator implements ConstraintValidator<Serializable, java.io.Serializable > {

	public void initialize(Serializable annotation) {
	}

	public boolean isValid( java.io.Serializable value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
