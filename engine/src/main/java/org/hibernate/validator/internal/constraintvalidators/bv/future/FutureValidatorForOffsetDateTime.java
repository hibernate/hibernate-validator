/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.spi.time.TimeProvider;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;
import java.time.OffsetDateTime;

/**
 * Check that the {@code java.time.OffsetDateTime} passed is in the future.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class FutureValidatorForOffsetDateTime implements ConstraintValidator<Future, OffsetDateTime> {

	@Override
	public void initialize(Future constraintAnnotation) {

	}

	@Override
	public boolean isValid(OffsetDateTime value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		TimeProvider timeProvider = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider();
		long now = timeProvider.getCurrentTime();

		return value.toInstant().toEpochMilli() > now;
	}
}
