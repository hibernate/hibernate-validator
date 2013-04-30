package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorcontext;

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

		boolean isValid;
		if ( caseMode == CaseMode.UPPER ) {
			isValid = object.equals( object.toUpperCase() );
		}
		else {
			isValid = object.equals( object.toLowerCase() );
		}

		if ( !isValid ) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(
					"{org.hibernate.validator.referenceguide.chapter06." +
					"constraintvalidatorcontext.CheckCase.message}"
			)
			.addConstraintViolation();
		}

		return isValid;
	}
}
