/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import java.util.Calendar;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

/**
 * Check that the <code>java.util.Calendar</code> passed to be validated is in
 * the future.
 *
 * @author Alaa Nassef
 */
public class FutureValidatorForCalendar implements ConstraintValidator<Future, Calendar> {

	public void initialize(Future constraintAnnotation) {
	}

	public boolean isValid(Calendar cal, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( cal == null ) {
			return true;
		}
		return cal.after( Calendar.getInstance() );
	}
}
