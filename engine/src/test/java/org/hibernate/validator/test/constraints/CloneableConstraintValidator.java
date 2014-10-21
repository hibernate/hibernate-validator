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
public class CloneableConstraintValidator implements ConstraintValidator<org.hibernate.validator.test.constraints.Cloneable, java.lang.Cloneable> {

	public void initialize(org.hibernate.validator.test.constraints.Cloneable annotation) {
	}

	public boolean isValid(java.lang.Cloneable value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
