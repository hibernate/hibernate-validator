/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Min;

/**
 * Check that the validated {@link Duration} is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class MinValidatorForDuration implements ConstraintValidator<Min, Duration> {

	private Duration duration;

	@Override
	public void initialize(Min minValue) {
		this.duration = Duration.of( minValue.value(), ChronoUnit.NANOS );
	}

	@Override
	public boolean isValid(Duration value, ConstraintValidatorContext constraintValidatorContext) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return duration.compareTo( value ) <= 0;
	}
}
