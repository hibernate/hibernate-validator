/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.Instant;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForInstant;

import static org.hibernate.validator.testutils.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForInstant}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForInstantTest {

	private FutureValidatorForInstant constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForInstant();
	}

	@Test
	public void testIsValid() {
		Instant future = Instant.now().plusSeconds( 3600 );
		Instant past = Instant.now().minusSeconds( 3600 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future Instant '" + future + "' fails validation." );
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past Instant '" + past + "' validated as future." );
	}
}
