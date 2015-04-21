/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.Year;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForYear;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForYear}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForYearTest {

	private FutureValidatorForYear constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForYear();
	}

	@Test
	public void testIsValid() {
		Year future = Year.now().plusYears( 1 );
		Year past = Year.now().minusYears( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future Year '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past Year '" + past + "' validated as future.");
	}
}
