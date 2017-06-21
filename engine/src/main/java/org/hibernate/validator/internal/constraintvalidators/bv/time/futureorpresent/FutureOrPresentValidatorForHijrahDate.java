/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.chrono.HijrahDate;

/**
 * Check that the {@code java.time.chrono.HijrahDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForHijrahDate extends AbstractFutureOrPresentJavaTimeValidator<HijrahDate> {

	@Override
	protected HijrahDate getReferenceValue(Clock reference) {
		return HijrahDate.now( reference );
	}

}
