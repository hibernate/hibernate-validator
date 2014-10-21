/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForChronoLocalDateTime;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForChronoLocalDateTime}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForChronoLocalDateTimeTest {

	private PastValidatorForChronoLocalDateTime constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForChronoLocalDateTime();
	}

	@Test
	public void testIsValid() {
		ChronoLocalDateTime future = LocalDateTime.now().plusDays( 1 );
		ChronoLocalDateTime past = LocalDateTime.now().minusDays( 1 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( past, null ), "Past LocalDateTime '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future LocalDateTime '" + future + "' validated as past.");
	}
}
