/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.AssertTrue;

/**
 * Validates that the value passed is true
 *
 * @author Alaa Nassef
 */
public class AssertTrueValidator implements ConstraintValidator<AssertTrue, Boolean> {

	public void initialize(AssertTrue constraintAnnotation) {
	}

	public boolean isValid(Boolean bool, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		return bool == null || bool;
	}

}
