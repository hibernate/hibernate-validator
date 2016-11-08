/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.LocalTime;

/**
 * Check that the {@code java.time.LocalTime} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForLocalTime extends AbstractPastJavaTimeValidator<LocalTime> {

	@Override
	protected LocalTime getReferenceValue(Clock reference) {
		return LocalTime.now( reference );
	}

}
