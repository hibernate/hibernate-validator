/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.OffsetTime;

/**
 * Check that the {@code java.time.OffsetTime} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForOffsetTime extends AbstractPastOrPresentJavaTimeValidator<OffsetTime> {

	@Override
	protected OffsetTime getReferenceValue(Clock reference) {
		return OffsetTime.now( reference );
	}

}
