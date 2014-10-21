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

/**
 * Check that the <code>java.util.Date</code> passed to be validated is in the
 * past.
 *
 * @author Alaa Nassef
 */
public class PastValidatorForDate implements ConstraintValidator<Past, Date> {

	public void initialize(Past constraintAnnotation) {
	}

	public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( date == null ) {
			return true;
		}
		return date.before( new Date() );
	}
}
