/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
