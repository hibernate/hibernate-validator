/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.LocalTime;

import javax.validation.constraints.FutureOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.LocalTime} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForLocalTime extends AbstractTemporalValidator<FutureOrPresent, LocalTime> {

	@Override
	protected LocalTime getReferenceValue(Clock reference) {
		return LocalTime.now( reference );
	}

}
