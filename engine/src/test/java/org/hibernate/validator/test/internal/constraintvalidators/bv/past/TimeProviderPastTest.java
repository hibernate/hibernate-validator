/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Past;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

/**
 * Test for using the {@code TimeProvider} contract in {@code @Past} validators.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-897")
public class TimeProviderPastTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private static ValidatorFactory validatorFactory;
	private Validator validator;

	@BeforeClass
	public static void setupValidatorFactory() {
		FixedDateTimeProvider timeProvider = new FixedDateTimeProvider(
				ZonedDateTime.of(
						1900, 2, 15, 4, 0, 0, 0,
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
	public void timeServiceIsUsedForPastOnCalendar() {
		Order order = new Order();
		order.orderDateAsCalendar = Calendar.getInstance();
		order.orderDateAsCalendar.set( 1901, 1, 15 );

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsCalendar" );
	}

	@Test
	public void timeServiceIsUsedForPastOnZonedDateTime() {
		Order order = new Order();
		order.orderDateAsZonedDateTime = ZonedDateTime.of( 1901, 2, 15, 4, 0, 0, 0, TZ_BERLIN );

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsZonedDateTime" );
	}

	@Test
	public void timeServiceIsUsedForPastOnDate() {
		Order order = new Order();

		Calendar date = Calendar.getInstance();
		date.set( 1901, 1, 15, 4, 0, 0 );
		order.orderDateAsDate = date.getTime();

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsDate" );
	}

	@Test
	public void timeServiceIsUsedForPastOnInstant() {
		Order order = new Order();
		order.orderDateAsInstant = ZonedDateTime.of( 1901, 2, 15, 4, 0, 0, 0, TZ_BERLIN ).toInstant();

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsInstant" );
	}

	@Test
	public void timeServiceIsUsedForPastOnOffsetDateTime() {
		Order order = new Order();
		order.orderDateAsOffsetDateTime = OffsetDateTime.ofInstant(
				ZonedDateTime.of( 1901, 2, 15, 4, 0, 0, 0, TZ_BERLIN ).toInstant(),
				TZ_BERLIN
		);

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsOffsetDateTime" );
	}

	@Test
	public void timeServiceIsUsedForPastOnReadableInstant() {
		Order order = new Order();
		order.orderDateAsReadableInstant = new DateTime( 1901, 2, 15, 4, 0, 0 );

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsReadableInstant" );
	}

	@Test
	public void timeServiceIsUsedForPastOnReadablePartial() {
		Order order = new Order();
		order.orderDateAsReadablePartial = new org.joda.time.LocalDateTime( 1901, 2, 15, 4, 0, 0 );

		assertCorrectPropertyPaths( validator.validate( order ), "orderDateAsReadablePartial" );
	}

	private static class Order {

		@Past
		private Calendar orderDateAsCalendar;

		@Past
		private ZonedDateTime orderDateAsZonedDateTime;

		@Past
		private Date orderDateAsDate;

		@Past
		private Instant orderDateAsInstant;

		@Past
		private OffsetDateTime orderDateAsOffsetDateTime;

		@Past
		private ReadableInstant orderDateAsReadableInstant;

		@Past
		private ReadablePartial orderDateAsReadablePartial;
	}

	private static class FixedDateTimeProvider implements TimeProvider {

		private final long fixedTime;

		private FixedDateTimeProvider(ZonedDateTime date) {
			this.fixedTime = GregorianCalendar.from( date ).getTimeInMillis();
		}

		@Override
		public long getCurrentTime() {
			return fixedTime;
		}
	}
}
