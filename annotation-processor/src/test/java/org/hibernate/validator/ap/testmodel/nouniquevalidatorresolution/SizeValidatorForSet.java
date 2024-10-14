/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidatorForSet implements ConstraintValidator<Size, Set> {

	@Override
	public void initialize(Size constraintAnnotation) {
	}

	@Override
	public boolean isValid(Set object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
