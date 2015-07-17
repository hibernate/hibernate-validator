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
public class SuperTypeArrayValidator implements ConstraintValidator<SuperTypeArray, SuperType[]> {

	public void initialize(SuperTypeArray annotation) {
	}

	public boolean isValid(SuperType[] value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
