/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06;

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

		if ( caseMode == CaseMode.UPPER ) {
			return object.equals( object.toUpperCase() );
		}
		else {
			return object.equals( object.toLowerCase() );
		}
	}
}
//end::include[]
