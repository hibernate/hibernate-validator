/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.chrono.ThaiBuddhistDate;

/**
 * Check that the {@code java.time.chrono.ThaiBuddhistDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForThaiBuddhistDate extends AbstractPastOrPresentJavaTimeValidator<ThaiBuddhistDate> {

	@Override
	protected ThaiBuddhistDate getReferenceValue(Clock reference) {
		return ThaiBuddhistDate.now( reference );
	}

}
