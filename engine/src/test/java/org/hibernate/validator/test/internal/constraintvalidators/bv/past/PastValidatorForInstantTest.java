/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.Instant;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForInstant;

import static org.hibernate.validator.testutils.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForInstant}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForInstantTest {

	private PastValidatorForInstant constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForInstant();
	}

	@Test
	public void testIsValid() {
		Instant future = Instant.now().plusSeconds( 3600 );
		Instant past = Instant.now().minusSeconds( 3600 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( past, getConstraintValidatorContext() ), "Past Instant '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, getConstraintValidatorContext() ), "Future Instant '" + future + "' validated as past.");
	}
}
