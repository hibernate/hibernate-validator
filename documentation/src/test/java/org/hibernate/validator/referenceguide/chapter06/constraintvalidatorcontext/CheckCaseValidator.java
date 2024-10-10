/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorcontext;

//end::include[]
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//tag::include[]
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
					"{org.hibernate.validator.referenceguide.chapter06."
							+ "constraintvalidatorcontext.CheckCase.message}"
			).addConstraintViolation();
		}

		return isValid;
	}
}
//end::include[]
