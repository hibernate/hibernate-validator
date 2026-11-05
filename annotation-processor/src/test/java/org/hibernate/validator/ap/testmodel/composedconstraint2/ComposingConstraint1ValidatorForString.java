package org.hibernate.validator.ap.testmodel.composedconstraint2;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ComposingConstraint1ValidatorForString implements ConstraintValidator<ComposingConstraint1, String> {

	@Override
	public void initialize(ComposingConstraint1 constraintAnnotation) {

	}

	@Override
	public boolean isValid(String object,
			ConstraintValidatorContext constraintContext) {
		return true;
	}

}
