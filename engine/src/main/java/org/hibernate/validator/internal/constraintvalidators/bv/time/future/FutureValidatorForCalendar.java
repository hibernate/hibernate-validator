/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.util.Calendar;

/**
 * Check that the <code>java.util.Calendar</code> passed to be validated is in
 * the future.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class FutureValidatorForCalendar extends AbstractFutureEpochBasedValidator<Calendar> {

	@Override
	protected long getEpochMillis(Calendar value, Clock reference) {
		return value.getTimeInMillis();
	}

}
