/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class AcmeConstraintValidator implements ConstraintValidator<AcmeConstraint, Object> {

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return false;
	}
}
