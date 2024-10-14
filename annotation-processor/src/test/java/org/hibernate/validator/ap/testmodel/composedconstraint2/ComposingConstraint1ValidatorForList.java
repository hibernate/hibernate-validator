/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ComposingConstraint1ValidatorForList implements ConstraintValidator<ComposingConstraint1, List<?>> {
	@Override
	public void initialize(ComposingConstraint1 constraintAnnotation) {

	}

	@Override
	public boolean isValid(List<?> object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
