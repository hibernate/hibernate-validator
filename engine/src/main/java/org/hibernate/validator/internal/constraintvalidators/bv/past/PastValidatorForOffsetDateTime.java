/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.past;

import java.time.OffsetDateTime;
import java.util.Calendar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;

/**
 * Check that the {@code java.time.OffsetDateTime} passed is in the past.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class PastValidatorForOffsetDateTime implements ConstraintValidator<Past, OffsetDateTime> {

	@Override
	public void initialize(Past constraintAnnotation) {

	}

	@Override
	public boolean isValid(OffsetDateTime value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		Calendar now = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider()
				.getCurrentTime();

		return value.isBefore( OffsetDateTime.ofInstant( now.toInstant(), now.getTimeZone().toZoneId() ) );
	}
}
