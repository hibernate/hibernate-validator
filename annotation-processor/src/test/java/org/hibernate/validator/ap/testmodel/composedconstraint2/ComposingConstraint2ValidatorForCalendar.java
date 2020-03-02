/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
