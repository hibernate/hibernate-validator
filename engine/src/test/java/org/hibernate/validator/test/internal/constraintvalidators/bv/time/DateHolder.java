/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import java.util.Calendar;
import java.util.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Past;

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
