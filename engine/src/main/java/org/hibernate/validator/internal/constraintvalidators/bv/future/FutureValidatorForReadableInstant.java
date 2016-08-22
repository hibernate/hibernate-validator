/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.time.TimeProvider;
import org.joda.time.ReadableInstant;

/**
 * Check if Joda Time type who implements {@code import org.joda.time.ReadableInstant} is in the future.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class FutureValidatorForReadableInstant implements ConstraintValidator<Future, ReadableInstant> {

	@Override
	public void initialize(Future constraintAnnotation) {
	}

	@Override
	public boolean isValid(ReadableInstant value, ConstraintValidatorContext context) {
		//null values are valid
		if ( value == null ) {
			return true;
		}

		TimeProvider timeProvider = context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider();
		long now = timeProvider.getCurrentTime();

		return value.getMillis() > now;
	}
}
