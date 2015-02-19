/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.past;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.joda.time.DateTime;
import org.joda.time.ReadablePartial;

/**
 * Check if Joda Time type who implements
 * {@code org.joda.time.ReadablePartial}
 * is in the past.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
@IgnoreJava6Requirement
public class PastValidatorForReadablePartial implements ConstraintValidator<Past, ReadablePartial> {

	@Override
	public void initialize(Past constraintAnnotation) {
	}

	@Override
	public boolean isValid(ReadablePartial value, ConstraintValidatorContext context) {
		//null values are valid
		if ( value == null ) {
			return true;
		}

		DateTime now = new DateTime(
				context.unwrap( HibernateConstraintValidatorContext.class )
				.getTimeProvider()
				.getCurrentTime()
				.getTime()
		);

		return value.toDateTime( now ).isBefore( now );
	}
}
