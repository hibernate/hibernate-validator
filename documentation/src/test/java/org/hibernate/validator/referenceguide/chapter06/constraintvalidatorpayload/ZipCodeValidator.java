/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//spotless:off
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload;

import jakarta.validation.ConstraintValidator;
//end::include[]
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

//spotless:on
//tag::include[]
public class ZipCodeValidator implements ConstraintValidator<ZipCode, String> {

	public String countryCode;

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		if ( object == null ) {
			return true;
		}

		boolean isValid = false;

		String countryCode = constraintContext
				.unwrap( HibernateConstraintValidatorContext.class )
				.getConstraintValidatorPayload( String.class );

		if ( "US".equals( countryCode ) ) {
			// checks specific to the United States
		}
		else if ( "FR".equals( countryCode ) ) {
			// checks specific to France
		}
		else {
			// ...
		}

		return isValid;
	}
}
//end::include[]
