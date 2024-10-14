/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

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
