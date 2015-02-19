/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;

/**
 * Check that the {@code java.time.chrono.ChronoZonedDateTime} passed is in the future.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class FutureValidatorForChronoZonedDateTime implements ConstraintValidator<Future, ChronoZonedDateTime<?>> {

	@Override
	public void initialize(Future constraintAnnotation) {

	}

	@Override
	public boolean isValid(ChronoZonedDateTime<?> value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		Instant now = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider()
				.getCurrentTime()
				.toInstant();

		return value.toInstant().isAfter( now );
	}
}
