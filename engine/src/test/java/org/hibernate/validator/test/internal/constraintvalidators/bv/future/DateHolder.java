/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.util.Calendar;
import java.util.Date;
import javax.validation.constraints.Future;
import javax.validation.constraints.Past;

/**
 * @author Hardy Ferentschik
 */
public class DateHolder {

	@Past
	private Calendar calendarWithPastDate;

	@Future
	private Calendar calendarWithFutureDate;

	@Past
	private Date past;

	@Past
	private Date future;

	public DateHolder() {
		calendarWithPastDate = Calendar.getInstance();
		calendarWithPastDate.add( Calendar.YEAR, -1 );
		past = calendarWithPastDate.getTime();

		calendarWithFutureDate = Calendar.getInstance();
		calendarWithFutureDate.add( Calendar.YEAR, 1 );
		future = calendarWithFutureDate.getTime();
	}

	public Calendar getCalendarWithPastDate() {
		return calendarWithPastDate;
	}

	public Calendar getCalendarWithFutureDate() {
		return calendarWithFutureDate;
	}

	public Date getPast() {
		return past;
	}

	public Date getFuture() {
		return future;
	}
}
