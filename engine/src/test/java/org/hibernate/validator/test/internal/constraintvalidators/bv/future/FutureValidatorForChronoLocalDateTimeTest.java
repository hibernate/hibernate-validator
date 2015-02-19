/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.LocalDateTime;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoLocalDateTime;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoLocalDateTime}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForChronoLocalDateTimeTest {

	private FutureValidatorForChronoLocalDateTime constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForChronoLocalDateTime();
	}

	@Test
	public void testIsValid() {
		LocalDateTime future = LocalDateTime.now().plusDays( 1 );
		LocalDateTime past = LocalDateTime.now().minusDays( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future LocalDateTime '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past LocalDateTime '" + past + "' validated as future.");
	}
}
