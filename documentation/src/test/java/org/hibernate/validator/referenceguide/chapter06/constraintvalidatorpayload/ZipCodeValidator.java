//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload;

//end::include[]

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

//tag::include[]
public class ZipCodeValidator implements HibernateConstraintValidator<ZipCode, String> {

	public String countryCode;

	@Override
	public void initialize(ConstraintDescriptor<ZipCode> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.countryCode = initializationContext
				.getConstraintValidatorPayload( String.class );
	}

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		if ( object == null ) {
			return true;
		}

		boolean isValid = false;

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
