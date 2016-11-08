/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.OffsetTime;

/**
 * Check that the {@code java.time.OffsetTime} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForOffsetTime extends AbstractFutureJavaTimeValidator<OffsetTime> {

	@Override
	protected OffsetTime getReferenceValue(Clock reference) {
		return OffsetTime.now( reference );
	}

}
