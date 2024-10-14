/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.util.Collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidatorForCollection implements ConstraintValidator<Size, Collection<?>> {

	@Override
	public void initialize(Size constraintAnnotation) {
	}

	@Override
	public boolean isValid(Collection<?> object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
