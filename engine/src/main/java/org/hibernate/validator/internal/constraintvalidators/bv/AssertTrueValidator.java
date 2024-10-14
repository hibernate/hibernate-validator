/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.AssertTrue;

/**
 * Validates that the value passed is true
 *
 * @author Alaa Nassef
 */
public class AssertTrueValidator implements ConstraintValidator<AssertTrue, Boolean> {

	@Override
	public boolean isValid(Boolean bool, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		return bool == null || bool;
	}

}
