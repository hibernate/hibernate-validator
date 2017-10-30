/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.chrono.MinguoDate;

import javax.validation.constraints.FutureOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.chrono.MinguoDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForMinguoDate extends AbstractTemporalValidator<FutureOrPresent, MinguoDate> {

	@Override
	protected MinguoDate getReferenceValue(Clock reference) {
		return MinguoDate.now( reference );
	}

}
