/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Past;

import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;

/**
 * Test for using the {@code ClockProvider} contract in {@code @Past} validators not covered by the TCK.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1135")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClockProviderPastTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private static ValidatorFactory validatorFactory;
	private Validator validator;

	@BeforeAll
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

	@BeforeEach
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
}
