/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

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
