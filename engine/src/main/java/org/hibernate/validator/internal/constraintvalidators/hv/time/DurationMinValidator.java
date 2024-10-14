/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.time;

import java.time.Duration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.time.DurationMin;

/**
 * Checks that a validated {@link Duration} length is longer than or equal to the
 * one specified with the annotation.
 *
 * @author Marko Bekhta
 */
public class DurationMinValidator implements ConstraintValidator<DurationMin, Duration> {

	private Duration minDuration;
	private boolean inclusive;

	@Override
	public void initialize(DurationMin constraintAnnotation) {
		this.minDuration = Duration.ofNanos( constraintAnnotation.nanos() )
				.plusMillis( constraintAnnotation.millis() )
				.plusSeconds( constraintAnnotation.seconds() )
				.plusMinutes( constraintAnnotation.minutes() )
				.plusHours( constraintAnnotation.hours() )
				.plusDays( constraintAnnotation.days() );
		this.inclusive = constraintAnnotation.inclusive();
	}

	@Override
	public boolean isValid(Duration value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}
		int comparisonResult = minDuration.compareTo( value );
		return inclusive ? comparisonResult <= 0 : comparisonResult < 0;
	}
}
