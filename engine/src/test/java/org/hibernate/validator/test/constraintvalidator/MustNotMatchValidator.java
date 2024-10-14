/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraintvalidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MustNotMatchValidator implements ConstraintValidator<MustNotMatch, String> {

	private String match;

	@Override
	public void initialize(MustNotMatch constraintAnnotation) {
		this.match = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		return !value.equals( match );
	}
}
