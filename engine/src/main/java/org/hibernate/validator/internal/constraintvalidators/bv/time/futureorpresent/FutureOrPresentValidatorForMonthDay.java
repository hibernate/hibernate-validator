/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Instant;
import java.time.MonthDay;

import javax.validation.constraints.FutureOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractInstantBasedValidator;

/**
 * Check that the {@code java.time.MonthDay} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForMonthDay extends AbstractInstantBasedValidator<FutureOrPresent, MonthDay> {

	@Override
	protected Instant getInstant(MonthDay value) {
		return (Instant) value.adjustInto( Instant.now( clockProvider.getClock() ) );
	}

}
