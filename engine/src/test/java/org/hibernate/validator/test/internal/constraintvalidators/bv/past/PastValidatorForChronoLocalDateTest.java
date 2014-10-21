/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForChronoLocalDate;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForChronoLocalDate}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForChronoLocalDateTest {

	private PastValidatorForChronoLocalDate constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForChronoLocalDate();
	}

	@Test
	public void testIsValidForNull() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );
	}

	@Test
	public void testIsValidForLocalDate() {
		ChronoLocalDate future = LocalDate.now().plusDays( 1 );
		ChronoLocalDate past = LocalDate.now().minusDays( 1 );

		assertTrue( constraint.isValid( past, null ), "Past LocalDate '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future LocalDate '" + future + "' validated as past.");
	}

	@Test
	public void testIsValidForJapaneseDate() {
		ChronoLocalDate future = JapaneseDate.now().plus( 1, DAYS );
		ChronoLocalDate past = JapaneseDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( past, null ), "Past JapaneseDate '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future JapaneseDate '" + future + "' validated as past.");
	}

	@Test
	public void testIsValidForHijrahDate() {
		ChronoLocalDate future = HijrahDate.now().plus( 1, DAYS );
		ChronoLocalDate past = HijrahDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( past, null ), "Past HijrahDate '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future HijrahDate '" + future + "' validated as past.");
	}

	@Test
	public void testIsValidForMinguoDate() {
		ChronoLocalDate future = MinguoDate.now().plus( 1, DAYS );
		ChronoLocalDate past = MinguoDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( past, null ), "Past MinguoDate '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future MinguoDate '" + future + "' validated as past.");
	}

	@Test
	public void testIsValidForThaiBuddhistDate() {
		ChronoLocalDate future = ThaiBuddhistDate.now().plus( 1, DAYS );
		ChronoLocalDate past = ThaiBuddhistDate.now().minus( 1, DAYS );

		assertTrue( constraint.isValid( past, null ), "Past ThaiBuddhistDate '" + past + "' fails validation.");
		assertFalse( constraint.isValid( future, null ), "Future ThaiBuddhistDate '" + future + "' validated as past.");
	}
}
