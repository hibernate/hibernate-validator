/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.context;

//end::include[]
import java.time.Instant;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Future;

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
