/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.GregorianCalendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComposingConstraint1ValidatorForGregorianCalendar
		implements ConstraintValidator<ComposingConstraint1, GregorianCalendar> {
	@Override
	public void initialize(ComposingConstraint1 constraintAnnotation) {
	}

	@Override
	public boolean isValid(GregorianCalendar object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
