/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.boxing;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidLongValidator implements ConstraintValidator<ValidLong, Long> {
	@Override
	public void initialize(ValidLong constraintAnnotation) {
	}

	@Override
	public boolean isValid(Long object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
