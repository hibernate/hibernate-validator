package org.hibernate.validator.referenceguide.chapter06.elinjection;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload.ZipCode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//tag::include[]
public class UnsafeValidator implements ConstraintValidator<ZipCode, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		context.disableDefaultConstraintViolation();

		HibernateConstraintValidatorContext hibernateContext = context.unwrap(
				HibernateConstraintValidatorContext.class );
		hibernateContext.disableDefaultConstraintViolation();

		if ( isInvalid( value ) ) {
			hibernateContext
					// THIS IS UNSAFE, DO NOT COPY THIS EXAMPLE
					.buildConstraintViolationWithTemplate( value + " is not a valid ZIP code" )
					.enableExpressionLanguage()
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
