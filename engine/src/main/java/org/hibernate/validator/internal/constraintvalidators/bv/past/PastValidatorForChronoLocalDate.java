/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.past;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Calendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.spi.time.TimeProvider;

/**
 * Check that the {@code java.time.chrono.ChronoLocalDate} passed is in the past.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class PastValidatorForChronoLocalDate implements ConstraintValidator<Past, ChronoLocalDate> {

	@Override
	public void initialize(Past constraintAnnotation) {

	}

	@Override
	public boolean isValid(ChronoLocalDate value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		TimeProvider timeProvider = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider();

		Calendar now = Calendar.getInstance();
		now.setTimeInMillis( timeProvider.getCurrentTime() );

		LocalDate today = LocalDateTime.ofInstant( now.toInstant(), now.getTimeZone().toZoneId() ).toLocalDate();

		return value.isBefore( today );
	}
}
