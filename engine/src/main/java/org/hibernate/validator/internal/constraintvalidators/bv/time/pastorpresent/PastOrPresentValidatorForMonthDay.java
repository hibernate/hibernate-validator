/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.MonthDay;

/**
 * Check that the {@code java.time.MonthDay} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForMonthDay extends AbstractPastOrPresentJavaTimeValidator<MonthDay> {

	@Override
	protected MonthDay getReferenceValue(Clock reference) {
		LocalDateTime now = LocalDateTime.now( reference ).plus( tolerance );
		return MonthDay.of( now.getMonth(), now.getDayOfMonth() );
	}

	@Override
	protected MonthDay adjustedReferenceValue(MonthDay value) {
		// value is already adjusted in the reference method
		return value;
	}

}
