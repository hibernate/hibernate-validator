/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

@SuppressWarnings("deprecation")
public class ModelWithJodaTypes {

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public Date jdkDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public GregorianCalendar jdkCalendar;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public ReadableInstant jodaInstant;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public DateMidnight jodaDateMidnight;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public ReadablePartial jodaPartial;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public LocalDate jodaLocalDate;

	/**
	 * Not allowed.
	 */
	@Future
	@Past
	@PastOrPresent
	@FutureOrPresent
	public String string;

}
