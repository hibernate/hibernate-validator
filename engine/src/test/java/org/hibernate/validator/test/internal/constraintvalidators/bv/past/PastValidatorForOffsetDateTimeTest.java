/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForOffsetDateTime;

import static org.hibernate.validator.testutils.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForOffsetDateTime}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForOffsetDateTimeTest {

	private PastValidatorForOffsetDateTime constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForOffsetDateTime();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );

		// Test allowed zone offsets (-18 to +18) with 1 hour increments
		for ( int i = -18; i <= 18; i++ ) {
			ZoneOffset offset = ZoneOffset.ofHours( i );
			OffsetDateTime future = OffsetDateTime.now( offset ).plusHours( 1 );
			OffsetDateTime past = OffsetDateTime.now( offset ).minusHours( 1 );
			assertTrue( constraint.isValid( past, getConstraintValidatorContext() ), "Past OffsetDateTime '" + past + "' fails validation." );
			assertFalse( constraint.isValid( future, getConstraintValidatorContext() ), "Future OffsetDateTime '" + future + "' validated as past." );
		}
	}
}
