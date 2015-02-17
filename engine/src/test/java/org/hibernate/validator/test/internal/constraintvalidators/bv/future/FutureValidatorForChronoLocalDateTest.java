/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoLocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoLocalDate}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForChronoLocalDateTest {

	private FutureValidatorForChronoLocalDate constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForChronoLocalDate();
	}

	@Test
	public void testIsValidForNull() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );
	}

	@Test
	public void testIsValidForLocalDate() {
		ChronoLocalDate future = LocalDate.now().plusDays( 1 );
		ChronoLocalDate past = LocalDate.now().minusDays( 1 );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future LocalDate '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past LocalDate '" + past + "' validated as future.");
	}

	@Test
	public void testIsValidForJapaneseDate() {
		ChronoLocalDate future = JapaneseDate.now().plus( 1, DAYS );
		ChronoLocalDate past = JapaneseDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future JapaneseDate '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past JapaneseDate '" + past + "' validated as future.");
	}

	@Test
	public void testIsValidForHijrahDate() {
		ChronoLocalDate future = HijrahDate.now().plus( 1, DAYS );
		ChronoLocalDate past = HijrahDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future HijrahDate '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past HijrahDate '" + past + "' validated as future.");
	}

	@Test
	public void testIsValidForMinguoDate() {
		ChronoLocalDate future = MinguoDate.now().plus( 1, DAYS );
		ChronoLocalDate past = MinguoDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future MinguoDate '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past MinguoDate '" + past + "' validated as future.");
	}

	@Test
	public void testIsValidForThaiBuddhistDate() {
		ChronoLocalDate future = ThaiBuddhistDate.now().plus( 1, DAYS );
		ChronoLocalDate past = ThaiBuddhistDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( future, getConstraintValidatorContext() ), "Future ThaiBuddhistDate '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, getConstraintValidatorContext() ), "Past ThaiBuddhistDate '" + past + "' validated as future.");
	}
}
