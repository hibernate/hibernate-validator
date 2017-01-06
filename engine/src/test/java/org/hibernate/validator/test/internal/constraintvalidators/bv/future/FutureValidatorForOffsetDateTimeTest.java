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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForOffsetDateTime;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForOffsetDateTime}.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class FutureValidatorForOffsetDateTimeTest {

	private FutureValidatorForOffsetDateTime constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForOffsetDateTime();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );

		// Test allowed zone offsets (-18 to +18) with 1 hour increments
		for ( int i = -18; i <= 18; i++ ) {
			ZoneOffset offset = ZoneOffset.ofHours( i );
			OffsetDateTime future = OffsetDateTime.now( offset ).plusHours( 1 );
			OffsetDateTime past = OffsetDateTime.now( offset ).minusHours( 1 );
			assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future OffsetDateTime '" + future + "' fails validation." );
			assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past OffsetDateTime '" + past + "' validated as future." );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1198")
	public void testEpochOverflow() {
		OffsetDateTime future = OffsetDateTime.MAX;

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future OffsetDateTime '" + future + "' fails validation." );
	}
}
