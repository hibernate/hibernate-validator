/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.chrono.MinguoDate;

/**
 * Check that the {@code java.time.chrono.MinguoDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForMinguoDate extends AbstractPastJavaTimeValidator<MinguoDate> {

	@Override
	protected MinguoDate getReferenceValue(Clock reference) {
		return MinguoDate.now( reference );
	}

}
