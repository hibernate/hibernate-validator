/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.validation.ClockProvider;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Past;

import org.hibernate.validator.testutil.TestForIssue;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for using the {@code ClockProvider} contract in {@code @Past} validators not covered by the TCK.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1135")
public class ClockProviderPastTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private static ValidatorFactory validatorFactory;
	private Validator validator;

	@BeforeClass
	public static void setupValidatorFactory() {
		FixedClockProvider clockProvider = new FixedClockProvider(
				ZonedDateTime.of(
						1900, 2, 15, 4, 0, 0, 0,
						TZ_BERLIN
				)
		);
		validatorFactory = getConfiguration()
				.clockProvider( clockProvider )
				.buildValidatorFactory();
	}

	@BeforeMethod
	public void setupValidator() {
		validator = validatorFactory.getValidator();
	}

	@Test
	public void clockProviderIsUsedForPastOnReadableInstant() {
		Order order = new Order();
		order.orderDateAsReadableInstant = new DateTime( 1901, 2, 15, 4, 0, 0 );

		assertThat( validator.validate( order ) ).containsOnlyViolations(
				violationOf( Past.class ).withProperty( "orderDateAsReadableInstant" )
		);
	}

	@Test
	public void clockProviderIsUsedForPastOnReadablePartial() {
		Order order = new Order();
		order.orderDateAsReadablePartial = new org.joda.time.LocalDateTime( 1901, 2, 15, 4, 0, 0 );

		assertThat( validator.validate( order ) ).containsOnlyViolations(
				violationOf( Past.class ).withProperty( "orderDateAsReadablePartial" )
		);
	}

	private static class Order {

		@Past
		private ReadableInstant orderDateAsReadableInstant;

		@Past
		private ReadablePartial orderDateAsReadablePartial;

	}

	private static class FixedClockProvider implements ClockProvider {

		private Clock clock;

		public FixedClockProvider(ZonedDateTime dateTime) {
			clock = Clock.fixed( dateTime.toInstant(), dateTime.getZone() );
		}

		@Override
		public Clock getClock() {
			return clock;
		}

	}
}
