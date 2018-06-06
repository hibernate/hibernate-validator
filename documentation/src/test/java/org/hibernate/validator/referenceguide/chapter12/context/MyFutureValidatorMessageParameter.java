//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.context;

import java.time.Instant;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

//tag::include[]
public class MyFutureValidatorMessageParameter implements ConstraintValidator<Future, Instant> {

	@Override
	public void initialize(Future constraintAnnotation) {
	}

	@Override
	public boolean isValid(Instant value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		HibernateConstraintValidatorContext hibernateContext = context.unwrap(
				HibernateConstraintValidatorContext.class
		);

		Instant now = Instant.now( context.getClockProvider().getClock() );

		if ( !value.isAfter( now ) ) {
			hibernateContext.disableDefaultConstraintViolation();
			hibernateContext
					.addMessageParameter( "now", now )
					.buildConstraintViolationWithTemplate( "Must be after {now}" )
					.addConstraintViolation();

			return false;
		}

		return true;
	}
}
//end::include[]
