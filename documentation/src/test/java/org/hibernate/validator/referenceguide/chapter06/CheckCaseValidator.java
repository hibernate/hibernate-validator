package org.hibernate.validator.referenceguide.chapter06;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckCaseValidator implements ConstraintValidator<CheckCase, String> {

	private CaseMode caseMode;

	@Override
	public void initialize(CheckCase constraintAnnotation) {
		this.caseMode = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		if ( object == null ) {
			return true;
		}

		if ( caseMode == CaseMode.UPPER ) {
			return object.equals( object.toUpperCase() );
		}
		else {
			return object.equals( object.toLowerCase() );
		}
	}
}
