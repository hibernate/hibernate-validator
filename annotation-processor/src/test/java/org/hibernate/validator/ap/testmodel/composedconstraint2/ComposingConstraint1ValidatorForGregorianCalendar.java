package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.GregorianCalendar;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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
