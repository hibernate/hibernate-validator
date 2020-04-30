package org.hibernate.validator.referenceguide.chapter06.elinjection;

import org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload.ZipCode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//tag::include[]
public class UnsafeValidator implements ConstraintValidator<ZipCode, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		context.disableDefaultConstraintViolation();

		if ( isInvalid( value ) ) {
			context
					.buildConstraintViolationWithTemplate( value + " is not a valid ZIP code" )
					.addConstraintViolation();

			return false;
		}

		return true;
	}

	private boolean isInvalid(String value) {
		// ...
		return false;
	}
}
// end::include[]
