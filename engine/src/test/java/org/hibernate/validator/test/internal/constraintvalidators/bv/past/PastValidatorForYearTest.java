/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.Year;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForYear;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForYear}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForYearTest {

	private PastValidatorForYear constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForYear();
	}

	@Test
	public void testIsValid() {
		Year future = Year.now().plusYears( 1 );
		Year past = Year.now().minusYears( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( past, null ), "Past Year '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future Year '" + future + "' validated as past.");
	}
}
