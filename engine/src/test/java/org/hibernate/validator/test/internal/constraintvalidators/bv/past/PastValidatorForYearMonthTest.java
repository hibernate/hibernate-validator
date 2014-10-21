/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.YearMonth;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForYearMonth;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForYearMonth}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForYearMonthTest {

	private PastValidatorForYearMonth constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForYearMonth();
	}

	@Test
	public void testIsValid() {
		YearMonth future = YearMonth.now().plusYears( 1 );
		YearMonth past = YearMonth.now().minusYears( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( past, null ), "Past YearMonth '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future YearMonth '" + future + "' validated as past.");
	}
}
