/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload;

import jakarta.validation.ConstraintValidator;
//end::include[]
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

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
