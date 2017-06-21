/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

/**
 * @author Khalid Alqinyah
 */
public class ModelWithJava8DateTime {

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public HijrahDate hijrahDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public Instant instant;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public JapaneseDate japaneseDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public LocalDate localDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public LocalDateTime localDateTime;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public LocalTime localTime;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public MinguoDate minguoDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public MonthDay monthDay;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public OffsetDateTime offsetDateTime;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public OffsetTime offsetTime;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public ThaiBuddhistDate thaiBuddhistDate;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public Year year;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public YearMonth yearMonth;

	@Past
	@Future
	@PastOrPresent
	@FutureOrPresent
	public ZonedDateTime zonedDateTime;
}
