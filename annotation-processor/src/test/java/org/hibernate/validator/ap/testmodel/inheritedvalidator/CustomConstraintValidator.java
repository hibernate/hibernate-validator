/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.inheritedvalidator;

import jakarta.validation.ConstraintValidatorContext;

public class CustomConstraintValidator extends AbstractCustomConstraintValidator {

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
