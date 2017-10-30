/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.OffsetTime;

import javax.validation.constraints.Future;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.OffsetTime} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForOffsetTime extends AbstractTemporalValidator<Future, OffsetTime> {

	@Override
	protected OffsetTime getReferenceValue(Clock reference) {
		return OffsetTime.now( reference );
	}

}
