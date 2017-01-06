/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import static org.hibernate.validator.testutils.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoZonedDateTime;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoZonedDateTime}.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class FutureValidatorForChronoZonedDateTimeTest {

	private FutureValidatorForChronoZonedDateTime constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForChronoZonedDateTime();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );

		// Test allowed zone offsets (UTC-18 to UTC+18) with 1 hour increments
		for ( int i = -18; i <= 18; i++ ) {
			ZoneId zone = ZoneId.ofOffset( "UTC", ZoneOffset.ofHours( i ) );
			ZonedDateTime future = ZonedDateTime.now( zone ).plusHours( 1 );
			ZonedDateTime past = ZonedDateTime.now( zone ).minusHours( 1 );
			assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future ZonedDateTime '" + future + "' fails validation." );
			assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past ZonedDateTime '" + past + "' validated as future." );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1198")
	public void testEpochOverflow() {
		ZonedDateTime future = ZonedDateTime.of( LocalDate.MAX, LocalTime.MAX, ZoneId.of( "GMT" ) );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future ZonedDateTime '" + future + "' fails validation." );
	}
}
