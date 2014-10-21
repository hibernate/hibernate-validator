/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.YearMonth;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForYearMonth;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForYearMonth}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForYearMonthTest {

	private FutureValidatorForYearMonth constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForYearMonth();
	}

	@Test
	public void testIsValid() {
		YearMonth future = YearMonth.now().plusYears( 1 );
		YearMonth past = YearMonth.now().minusYears( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( future, null ), "Future YearMonth '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, null ), "Past YearMonth '" + past + "' validated as future.");
	}
}
