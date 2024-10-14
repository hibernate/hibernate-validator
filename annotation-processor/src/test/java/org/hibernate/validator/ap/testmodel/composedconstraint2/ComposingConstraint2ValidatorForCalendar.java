/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.Calendar;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ComposingConstraint2ValidatorForCalendar implements ConstraintValidator<ComposingConstraint2, Calendar> {

	@Override
	public void initialize(ComposingConstraint2 constraintAnnotation) {

	}

	@Override
	public boolean isValid(Calendar object,
			ConstraintValidatorContext constraintContext) {
		return true;
	}

}
