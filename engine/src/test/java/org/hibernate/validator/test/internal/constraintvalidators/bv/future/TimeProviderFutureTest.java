/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Future;

import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.testutil.TestForIssue;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for using the {@code TimeProvider} contract in {@code @Future} validators.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-897")
public class TimeProviderFutureTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private static ValidatorFactory validatorFactory;
	private Validator validator;

	@BeforeClass
	public static void setupValidatorFactory() {
		FixedDateTimeProvider timeProvider = new FixedDateTimeProvider(
				ZonedDateTime.of(
						2100, 2, 15, 4, 0, 0, 0,
						TZ_BERLIN
				)
		);
		validatorFactory = getConfiguration()
				.timeProvider( timeProvider )
				.buildValidatorFactory();
	}

	@BeforeMethod
	public void setupValidator() {
		validator = validatorFactory.getValidator();
	}

	@Test
	public void timeServiceIsUsedForFutureOnCalendar() {
		Order order = new Order();
		order.shipmentDateAsCalendar = Calendar.getInstance();
		order.shipmentDateAsCalendar.set( 2099, 1, 15 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsCalendar" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnLocalDate() {
		Order order = new Order();
		order.shipmentDateAsLocalDate = LocalDate.of( 2099, 2, 15 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsLocalDate" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnLocalDateTime() {
		Order order = new Order();
		order.shipmentDateAsLocalDateTime = LocalDateTime.of( 2099, 2, 15, 4, 0, 0 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsLocalDateTime" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnZonedDateTime() {
		Order order = new Order();
		order.shipmentDateAsZonedDateTime = ZonedDateTime.of( 2099, 2, 15, 4, 0, 0, 0, TZ_BERLIN );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsZonedDateTime" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnDate() {
		Order order = new Order();

		Calendar date = Calendar.getInstance();
		date.set( 2099, 1, 15, 4, 0, 0 );
		order.shipmentDateAsDate = date.getTime();

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsDate" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnInstant() {
		Order order = new Order();
		order.shipmentDateAsInstant = ZonedDateTime.of( 2099, 2, 15, 4, 0, 0, 0, TZ_BERLIN ).toInstant();

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsInstant" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnOffsetDateTime() {
		Order order = new Order();
		order.shipmentDateAsOffsetDateTime = OffsetDateTime.ofInstant(
				ZonedDateTime.of( 2099, 2, 15, 4, 0, 0, 0, TZ_BERLIN ).toInstant(),
				TZ_BERLIN
		);

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsOffsetDateTime" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnReadableInstant() {
		Order order = new Order();
		order.shipmentDateAsReadableInstant = new DateTime( 2099, 2, 15, 4, 0, 0 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsReadableInstant" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnReadablePartial() {
		Order order = new Order();
		order.shipmentDateAsReadablePartial = new org.joda.time.LocalDateTime( 2099, 2, 15, 4, 0, 0 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsReadablePartial" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnYear() {
		Order order = new Order();
		order.shipmentDateAsYear = Year.of( 2099 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsYear" );
	}

	@Test
	public void timeServiceIsUsedForFutureOnYearMonth() {
		Order order = new Order();
		order.shipmentDateAsYearMonth = YearMonth.of( 2099, 2 );

		assertCorrectPropertyPaths( validator.validate( order ), "shipmentDateAsYearMonth" );
	}

	private static class Order {

		@Future
		private Calendar shipmentDateAsCalendar;

		@Future
		private LocalDate shipmentDateAsLocalDate;

		@Future
		private LocalDateTime shipmentDateAsLocalDateTime;

		@Future
		private ZonedDateTime shipmentDateAsZonedDateTime;

		@Future
		private Date shipmentDateAsDate;

		@Future
		private Instant shipmentDateAsInstant;

		@Future
		private OffsetDateTime shipmentDateAsOffsetDateTime;

		@Future
		private ReadableInstant shipmentDateAsReadableInstant;

		@Future
		private ReadablePartial shipmentDateAsReadablePartial;

		@Future
		private Year shipmentDateAsYear;

		@Future
		private YearMonth shipmentDateAsYearMonth;
	}

	private static class FixedDateTimeProvider implements TimeProvider {

		private final GregorianCalendar now;

		private FixedDateTimeProvider(ZonedDateTime date) {
			this.now = GregorianCalendar.from( date );
		}

		@Override
		public Calendar getCurrentTime() {
			return now;
		}
	}
}
