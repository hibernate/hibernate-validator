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
import javax.validation.constraints.Past;

/**
 * @author Khalid Alqinyah
 */
public class ModelWithJava8DateTime {

	@Past
	@Future
	public HijrahDate hijrahDate;

	@Past
	@Future
	public Instant instant;

	@Past
	@Future
	public JapaneseDate japaneseDate;

	@Past
	@Future
	public LocalDate localDate;

	@Past
	@Future
	public LocalDateTime localDateTime;

	@Past
	@Future
	public LocalTime localTime;

	@Past
	@Future
	public MinguoDate minguoDate;

	@Past
	@Future
	public MonthDay monthDay;

	@Past
	@Future
	public OffsetDateTime offsetDateTime;

	@Past
	@Future
	public OffsetTime offsetTime;

	@Past
	@Future
	public ThaiBuddhistDate thaiBuddhistDate;

	@Past
	@Future
	public Year year;

	@Past
	@Future
	public YearMonth yearMonth;

	@Past
	@Future
	public ZonedDateTime zonedDateTime;
}
