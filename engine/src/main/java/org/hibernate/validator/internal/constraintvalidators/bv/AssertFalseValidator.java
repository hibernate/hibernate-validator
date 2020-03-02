/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.AssertFalse;

/**
 * Validates that the value passed is false
 *
 * @author Alaa Nassef
 */
public class AssertFalseValidator implements ConstraintValidator<AssertFalse, Boolean> {

	@Override
	public boolean isValid(Boolean bool, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		return bool == null || !bool;
	}

}
