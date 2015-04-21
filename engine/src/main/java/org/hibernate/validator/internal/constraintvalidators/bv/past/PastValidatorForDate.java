/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.past;

import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.time.TimeProvider;

/**
 * Check that the <code>java.util.Date</code> passed to be validated is in the
 * past.
 *
 * @author Alaa Nassef
 */
public class PastValidatorForDate implements ConstraintValidator<Past, Date> {

	@Override
	public void initialize(Past constraintAnnotation) {
	}

	@Override
	public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( date == null ) {
			return true;
		}

		TimeProvider timeProvider = constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider();
		long now = timeProvider.getCurrentTime();

		return date.getTime() < now;
	}
}
