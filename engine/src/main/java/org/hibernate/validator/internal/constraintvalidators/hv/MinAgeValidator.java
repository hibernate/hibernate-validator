/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.MinAge;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 *
 * Checks that the number of years from a given date to today is greater or equal to
 * a specified value
 *
 * @author Hillmer Chona
 * @since 6.0.x
 */


public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

	private long minAge;

	@Override
	public void initialize(MinAge minAgeValue) {
		this.minAge = minAgeValue.value();
	}

	@Override
	public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
		// null values are valid
		if ( date == null ) {
			return true;
		}

		return ChronoUnit.YEARS.between( date, LocalDate.now() ) >= minAge;
	}
}
