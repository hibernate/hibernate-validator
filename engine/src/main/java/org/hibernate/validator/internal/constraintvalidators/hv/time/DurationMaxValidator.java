/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.time;

import java.time.Duration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.time.DurationMax;

/**
 * Checks that a validated {@link Duration} length is shorter than or equal to the
 * one specified with the annotation.
 *
 * @author Marko Bekhta
 */
public class DurationMaxValidator implements ConstraintValidator<DurationMax, Duration> {

	private Duration maxDuration;
	private boolean inclusive;

	@Override
	public void initialize(DurationMax constraintAnnotation) {
		this.maxDuration = Duration.ofNanos( constraintAnnotation.nanos() )
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
		int comparisonResult = maxDuration.compareTo( value );
		return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
	}
}
