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
import javax.validation.constraints.Past;

import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

public class ModelWithJodaTypes {

	@Past
	@Future
	public Date jdkDate;
	
	@Past
	@Future
	public GregorianCalendar jdkCalendar;
	
	@Past
	@Future
	public ReadableInstant jodaInstant;

	@Past
	@Future
	public DateMidnight jodaDateMidnight;
	
	@Past
	@Future
	public ReadablePartial jodaPartial;

	@Past
	@Future
	public LocalDate jodaLocalDate;
	
	/**
	 * Not allowed.
	 */
	@Future
	@Past
	public String string;
	
}
