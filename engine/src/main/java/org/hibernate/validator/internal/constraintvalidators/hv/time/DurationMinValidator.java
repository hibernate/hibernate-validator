/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.time;

import java.time.Duration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.time.DurationMin;

/**
 * Checks that a validated {@link Duration} length is greater than or equals to the
 * one specified with the annotation.
 *
 * @author Marko Bekhta
 */
public class DurationMinValidator implements ConstraintValidator<DurationMin, Duration> {

	private Duration duration;

	@Override
	public void initialize(DurationMin constraintAnnotation) {
		this.duration = Duration.of( constraintAnnotation.value(), constraintAnnotation.units() );
	}

	@Override
	public boolean isValid(Duration value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}
		return duration.compareTo( value ) < 1;
	}
}
