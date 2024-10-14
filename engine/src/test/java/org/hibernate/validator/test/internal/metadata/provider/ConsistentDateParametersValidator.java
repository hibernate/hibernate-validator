/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.provider;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

/**
 * @author Gunnar Morling
 */
@SupportedValidationTarget(value = ValidationTarget.PARAMETERS)
public class ConsistentDateParametersValidator implements ConstraintValidator<ConsistentDateParameters, Object[]> {

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		return false;
	}
}
