/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.validation.Validator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import org.hibernate.validator.testutil.TestForIssue;
import org.joda.time.ReadableInstant;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1493")
public class TemporalValidationToleranceTest {

	@Test
	public void testFutureTolerance() throws Exception {
		ZonedDateTime reference = ZonedDateTime.of( 2017, 1, 1, 0, 0, 7, 0, ZoneId.systemDefault() );
		FutureDummyEntity entity = new FutureDummyEntity( reference.minus( Duration.ofSeconds( 5 ) ) );

		Validator validator = getNoTemporalValidationToleranceValidator( reference );

		assertThat( validator.validate( entity ) ).containsOnlyViolations(
				violationOf( Future.class ).withProperty( "calendar" ),
				violationOf( Future.class ).withProperty( "date" ),
				violationOf( Future.class ).withProperty( "hijrahDate" ),
				violationOf( Future.class ).withProperty( "instant" ),
				violationOf( Future.class ).withProperty( "japaneseDate" ),
				violationOf( Future.class ).withProperty( "localDate" ),
				violationOf( Future.class ).withProperty( "localDateTime" ),
				violationOf( Future.class ).withProperty( "minguoDate" ),
				violationOf( Future.class ).withProperty( "offsetDateTime" ),
				violationOf( Future.class ).withProperty( "thaiBuddhistDate" ),
				violationOf( Future.class ).withProperty( "year" ),
				violationOf( Future.class ).withProperty( "yearMonth" ),
				violationOf( Future.class ).withProperty( "zonedDateTime" ),
				violationOf( Future.class ).withProperty( "readableInstant" )
		);

		validator = getTenSecondsTemporalValidationToleranceValidator( reference );

		assertNoViolations( validator.validate( entity ) );
	}

	@Test
	public void testFutureOrPresentTolerance() throws Exception {
		ZonedDateTime reference = ZonedDateTime.of( 2017, 1, 1, 0, 0, 7, 0, ZoneId.systemDefault() );
		FutureOrPresentDummyEntity entity = new FutureOrPresentDummyEntity( reference.minus( Duration.ofDays( 600 ) ) );

		Validator validator = getNoTemporalValidationToleranceValidator( reference );

		assertThat( validator.validate( entity ) ).containsOnlyViolations(
				violationOf( FutureOrPresent.class ).withProperty( "calendar" ),
				violationOf( FutureOrPresent.class ).withProperty( "date" ),
				violationOf( FutureOrPresent.class ).withProperty( "hijrahDate" ),
				violationOf( FutureOrPresent.class ).withProperty( "instant" ),
				violationOf( FutureOrPresent.class ).withProperty( "japaneseDate" ),
				violationOf( FutureOrPresent.class ).withProperty( "localDate" ),
				violationOf( FutureOrPresent.class ).withProperty( "localDateTime" ),
				violationOf( FutureOrPresent.class ).withProperty( "minguoDate" ),
				violationOf( FutureOrPresent.class ).withProperty( "offsetDateTime" ),
				violationOf( FutureOrPresent.class ).withProperty( "thaiBuddhistDate" ),
				violationOf( FutureOrPresent.class ).withProperty( "year" ),
				violationOf( FutureOrPresent.class ).withProperty( "yearMonth" ),
				violationOf( FutureOrPresent.class ).withProperty( "zonedDateTime" ),
				violationOf( FutureOrPresent.class ).withProperty( "readableInstant" )
		);

		validator = get2YearsTemporalValidationToleranceValidator( reference );

		assertNoViolations( validator.validate( entity ) );
	}

	@Test
	public void testPastTolerance() throws Exception {
		ZonedDateTime reference = ZonedDateTime.of( 2017, 12, 31, 23, 59, 53, 0, ZoneId.systemDefault() );
		PastDummyEntity entity = new PastDummyEntity( reference.plus( Duration.ofSeconds( 5 ) ) );

		Validator validator = getNoTemporalValidationToleranceValidator( reference );

		assertThat( validator.validate( entity ) ).containsOnlyViolations(
				violationOf( Past.class ).withProperty( "calendar" ),
				violationOf( Past.class ).withProperty( "date" ),
				violationOf( Past.class ).withProperty( "hijrahDate" ),
				violationOf( Past.class ).withProperty( "instant" ),
				violationOf( Past.class ).withProperty( "japaneseDate" ),
				violationOf( Past.class ).withProperty( "localDate" ),
				violationOf( Past.class ).withProperty( "localDateTime" ),
				violationOf( Past.class ).withProperty( "minguoDate" ),
				violationOf( Past.class ).withProperty( "offsetDateTime" ),
				violationOf( Past.class ).withProperty( "thaiBuddhistDate" ),
				violationOf( Past.class ).withProperty( "year" ),
				violationOf( Past.class ).withProperty( "yearMonth" ),
				violationOf( Past.class ).withProperty( "zonedDateTime" ),
				violationOf( Past.class ).withProperty( "readableInstant" )
		);

		validator = getTenSecondsTemporalValidationToleranceValidator( reference );

		assertNoViolations( validator.validate( entity ) );
	}

	@Test
	public void testPastOrPresentTolerance() throws Exception {
		ZonedDateTime reference = ZonedDateTime.of( 2017, 1, 1, 0, 0, 7, 0, ZoneId.systemDefault() );
		PastOrPresentDummyEntity entity = new PastOrPresentDummyEntity( reference.plus( Duration.ofDays( 600 ) ) );

		Validator validator = getNoTemporalValidationToleranceValidator( reference );

		assertThat( validator.validate( entity ) ).containsOnlyViolations(
				violationOf( PastOrPresent.class ).withProperty( "calendar" ),
				violationOf( PastOrPresent.class ).withProperty( "date" ),
				violationOf( PastOrPresent.class ).withProperty( "hijrahDate" ),
				violationOf( PastOrPresent.class ).withProperty( "instant" ),
				violationOf( PastOrPresent.class ).withProperty( "japaneseDate" ),
				violationOf( PastOrPresent.class ).withProperty( "localDate" ),
				violationOf( PastOrPresent.class ).withProperty( "localDateTime" ),
				violationOf( PastOrPresent.class ).withProperty( "minguoDate" ),
				violationOf( PastOrPresent.class ).withProperty( "offsetDateTime" ),
				violationOf( PastOrPresent.class ).withProperty( "thaiBuddhistDate" ),
				violationOf( PastOrPresent.class ).withProperty( "year" ),
				violationOf( PastOrPresent.class ).withProperty( "yearMonth" ),
				violationOf( PastOrPresent.class ).withProperty( "zonedDateTime" ),
				violationOf( PastOrPresent.class ).withProperty( "readableInstant" )
		);

		validator = get2YearsTemporalValidationToleranceValidator( reference );

		assertNoViolations( validator.validate( entity ) );
	}

	private Validator getNoTemporalValidationToleranceValidator(ZonedDateTime reference) {
		return getConfiguration()
				.clockProvider( () -> Clock.fixed( reference.toInstant(), reference.getZone() ) )
				.buildValidatorFactory().getValidator();
	}

	private Validator getTenSecondsTemporalValidationToleranceValidator(ZonedDateTime reference) {
		return getConfiguration()
				.temporalValidationTolerance( Duration.ofSeconds( 10 ) )
				.clockProvider( () -> Clock.fixed( reference.toInstant(), reference.getZone() ) )
				.buildValidatorFactory().getValidator();
	}

	private Validator get2YearsTemporalValidationToleranceValidator(ZonedDateTime reference) {
		return getConfiguration()
				.temporalValidationTolerance( Duration.ofDays( 365 * 2 ) )
				.clockProvider( () -> Clock.fixed( reference.toInstant(), reference.getZone() ) )
				.buildValidatorFactory().getValidator();
	}

	private static class FutureDummyEntity {

		@Future
		private Calendar calendar;

		@Future
		private Date date;

		@Future
		private HijrahDate hijrahDate;

		@Future
		private Instant instant;

		@Future
		private JapaneseDate japaneseDate;

		@Future
		private LocalDate localDate;

		@Future
		private LocalDateTime localDateTime;

		@Future
		private MinguoDate minguoDate;

		@Future
		private OffsetDateTime offsetDateTime;

		@Future
		private ThaiBuddhistDate thaiBuddhistDate;

		@Future
		private Year year;

		@Future
		private YearMonth yearMonth;

		@Future
		private ZonedDateTime zonedDateTime;

		@Future
		private ReadableInstant readableInstant;

		public FutureDummyEntity(ZonedDateTime dateTime) {
			calendar = GregorianCalendar.from( dateTime );
			date = calendar.getTime();

			instant = dateTime.toInstant();
			localDateTime = dateTime.toLocalDateTime();

			hijrahDate = HijrahDate.from( dateTime );
			japaneseDate = JapaneseDate.from( dateTime );
			localDate = LocalDate.from( dateTime );
			minguoDate = MinguoDate.from( dateTime );
			offsetDateTime = dateTime.toOffsetDateTime();
			thaiBuddhistDate = ThaiBuddhistDate.from( dateTime );
			year = Year.from( dateTime );
			yearMonth = YearMonth.from( dateTime );
			zonedDateTime = dateTime;
			readableInstant = new org.joda.time.DateTime( dateTime.toEpochSecond() * 1_000 );
		}
	}

	private static class FutureOrPresentDummyEntity {

		@FutureOrPresent
		private Calendar calendar;

		@FutureOrPresent
		private Date date;

		@FutureOrPresent
		private HijrahDate hijrahDate;

		@FutureOrPresent
		private Instant instant;

		@FutureOrPresent
		private JapaneseDate japaneseDate;

		@FutureOrPresent
		private LocalDate localDate;

		@FutureOrPresent
		private LocalDateTime localDateTime;

		@FutureOrPresent
		private MinguoDate minguoDate;

		@FutureOrPresent
		private OffsetDateTime offsetDateTime;

		@FutureOrPresent
		private ThaiBuddhistDate thaiBuddhistDate;

		@FutureOrPresent
		private Year year;

		@FutureOrPresent
		private YearMonth yearMonth;

		@FutureOrPresent
		private ZonedDateTime zonedDateTime;

		@FutureOrPresent
		private ReadableInstant readableInstant;

		public FutureOrPresentDummyEntity(ZonedDateTime dateTime) {
			calendar = GregorianCalendar.from( dateTime );
			date = calendar.getTime();

			instant = dateTime.toInstant();
			localDateTime = dateTime.toLocalDateTime();

			hijrahDate = HijrahDate.from( dateTime );
			japaneseDate = JapaneseDate.from( dateTime );
			localDate = LocalDate.from( dateTime );
			minguoDate = MinguoDate.from( dateTime );
			offsetDateTime = dateTime.toOffsetDateTime();
			thaiBuddhistDate = ThaiBuddhistDate.from( dateTime );
			year = Year.from( dateTime );
			yearMonth = YearMonth.from( dateTime );
			zonedDateTime = dateTime;
			readableInstant = new org.joda.time.DateTime( dateTime.toEpochSecond() * 1_000 );
		}
	}

	private static class PastDummyEntity {

		@Past
		private Calendar calendar;

		@Past
		private Date date;

		@Past
		private HijrahDate hijrahDate;

		@Past
		private Instant instant;

		@Past
		private JapaneseDate japaneseDate;

		@Past
		private LocalDate localDate;

		@Past
		private LocalDateTime localDateTime;

		@Past
		private MinguoDate minguoDate;

		@Past
		private OffsetDateTime offsetDateTime;

		@Past
		private ThaiBuddhistDate thaiBuddhistDate;

		@Past
		private Year year;

		@Past
		private YearMonth yearMonth;

		@Past
		private ZonedDateTime zonedDateTime;

		@Past
		private ReadableInstant readableInstant;

		public PastDummyEntity(ZonedDateTime dateTime) {
			calendar = GregorianCalendar.from( dateTime );
			date = calendar.getTime();

			instant = dateTime.toInstant();
			localDateTime = dateTime.toLocalDateTime();

			hijrahDate = HijrahDate.from( dateTime );
			japaneseDate = JapaneseDate.from( dateTime );
			localDate = LocalDate.from( dateTime );
			minguoDate = MinguoDate.from( dateTime );
			offsetDateTime = dateTime.toOffsetDateTime();
			thaiBuddhistDate = ThaiBuddhistDate.from( dateTime );
			year = Year.from( dateTime );
			yearMonth = YearMonth.from( dateTime );
			zonedDateTime = dateTime;
			readableInstant = new org.joda.time.DateTime( dateTime.toEpochSecond() * 1_000 );
		}
	}

	private static class PastOrPresentDummyEntity {

		@PastOrPresent
		private Calendar calendar;

		@PastOrPresent
		private Date date;

		@PastOrPresent
		private HijrahDate hijrahDate;

		@PastOrPresent
		private Instant instant;

		@PastOrPresent
		private JapaneseDate japaneseDate;

		@PastOrPresent
		private LocalDate localDate;

		@PastOrPresent
		private LocalDateTime localDateTime;

		@PastOrPresent
		private MinguoDate minguoDate;

		@PastOrPresent
		private OffsetDateTime offsetDateTime;

		@PastOrPresent
		private ThaiBuddhistDate thaiBuddhistDate;

		@PastOrPresent
		private Year year;

		@PastOrPresent
		private YearMonth yearMonth;

		@PastOrPresent
		private ZonedDateTime zonedDateTime;

		@PastOrPresent
		private ReadableInstant readableInstant;

		public PastOrPresentDummyEntity(ZonedDateTime dateTime) {
			calendar = GregorianCalendar.from( dateTime );
			date = calendar.getTime();

			instant = dateTime.toInstant();
			localDateTime = dateTime.toLocalDateTime();

			hijrahDate = HijrahDate.from( dateTime );
			japaneseDate = JapaneseDate.from( dateTime );
			localDate = LocalDate.from( dateTime );
			minguoDate = MinguoDate.from( dateTime );
			offsetDateTime = dateTime.toOffsetDateTime();
			thaiBuddhistDate = ThaiBuddhistDate.from( dateTime );
			year = Year.from( dateTime );
			yearMonth = YearMonth.from( dateTime );
			zonedDateTime = dateTime;
			readableInstant = new org.joda.time.DateTime( dateTime.toEpochSecond() * 1_000 );
		}
	}
}
