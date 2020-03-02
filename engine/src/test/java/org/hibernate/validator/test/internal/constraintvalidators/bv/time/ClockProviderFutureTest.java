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
import jakarta.validation.constraints.Future;

import org.hibernate.validator.testutil.TestForIssue;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for using the {@code ClockProvider} contract in {@code @Future} validators not covered by the TCK.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-897")
public class ClockProviderFutureTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private static ValidatorFactory validatorFactory;
	private Validator validator;

	@BeforeClass
	public static void setupValidatorFactoryAndValidationXmlTestHelper() {
		FixedClockProvider clockProvider = new FixedClockProvider(
				ZonedDateTime.of(
						2100, 2, 15, 4, 0, 0, 0,
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
	public void clockProviderIsUsedForFutureOnReadableInstant() {
		Order order = new Order();
		order.shipmentDateAsReadableInstant = new DateTime( 2099, 2, 15, 4, 0, 0 );

		assertThat( validator.validate( order ) ).containsOnlyViolations(
				violationOf( Future.class ).withProperty( "shipmentDateAsReadableInstant" )
		);
	}

	@Test
	public void clockProviderIsUsedForFutureOnReadablePartial() {
		Order order = new Order();
		order.shipmentDateAsReadablePartial = new org.joda.time.LocalDateTime( 2099, 2, 15, 4, 0, 0 );

		assertThat( validator.validate( order ) ).containsOnlyViolations(
				violationOf( Future.class ).withProperty( "shipmentDateAsReadablePartial" )
		);
	}

	private static class Order {

		@Future
		private ReadableInstant shipmentDateAsReadableInstant;

		@Future
		private ReadablePartial shipmentDateAsReadablePartial;

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
