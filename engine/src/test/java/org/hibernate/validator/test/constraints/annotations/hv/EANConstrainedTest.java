/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutils.LuhnTestUtil.RepeatedDigits;
import org.hibernate.validator.testutils.Mod10TestUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class EANConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testEANNumber() {
		Foo foo = new Foo( "4006381333931" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testEANInvalid() {
		Foo foo = new Foo( "400638133393" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Foo.class )
				.getConstraintsForProperty( "number" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "eanNumbers")
	public void testEANBoundaries(CharSequence number, boolean valid8, boolean valid13) {
		assertEquals( validator.validateValue( EANNumbers.class, "ean8", number ).isEmpty(), valid8 );
		assertEquals( validator.validateValue( EANNumbers.class, "ean13", number ).isEmpty(), valid13 );
	}

	@DataProvider
	public Object[][] eanNumbers() {
		return new Object[][] {
				{ null, true, true },
				{ "", false, false },
				{ "73513537", true, false },
				{ "73513538", false, false },
				{ "4006381333931", false, true },
				{ "4006381333932", false, false },
				{ "00000000", true, false },
				{ "0000000000000", false, true },
				{ "-------0", true, false },
				{ "aaaaaaaaaaaa0", false, true },
				{ "--73-530", true, false },
				{ "７３５１３５３７", false, false },
				{ "７３５１３５３０", true, false },
				{ "400638133393１", false, true },
				{ "0000000٠", true, false },
				{ "7351353 ", false, false },
				{ "400638133393-", false, false },
				{ "73513537\n", false, false },
				{ "7351353", false, false },
				{ "400638133393", false, false },
				{ new StringBuilder( "73513537" ), true, false },
				{ new StringBuilder( "4006381333931" ), false, true },
				{ "0".repeat( 100_000 ), false, false }
		};
	}

	@Test
	public void testMatchesIndependentLegacyMod10Reference() {
		Random random = new Random( 799 );
		String mixedCharacters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( int size : new int[] { 8, 13 } ) {
			for ( int sample = 0; sample < 300; sample++ ) {
				StringBuilder number = new StringBuilder();
				for ( int i = 0; i < size; i++ ) {
					number.append( sample % 3 == 0
							? (char) ( '0' + random.nextInt( 10 ) )
							: mixedCharacters.charAt( random.nextInt( mixedCharacters.length() ) ) );
				}
				assertEquals( validator.validateValue( EANNumbers.class, size == 8 ? "ean8" : "ean13", number ).isEmpty(),
						Mod10TestUtil.isValidLegacy( number, 0, Integer.MAX_VALUE, -1, true, 3, 1 ), number.toString() );
			}
		}
	}

	@Test
	public void testCharacterSequenceDoesNotRequireStringConversion() {
		assertTrue( validator.validateValue( EANNumbers.class, "ean8", new RepeatedDigits( 7, '0', '0' ) ).isEmpty() );
		assertTrue( validator.validateValue( EANNumbers.class, "ean13", new RepeatedDigits( 12, '0', '0' ) ).isEmpty() );
	}

	private static class EANNumbers {
		@EAN(type = EAN.Type.EAN8)
		private CharSequence ean8;

		@EAN
		private CharSequence ean13;
	}

	private static class Foo {

		@EAN
		private final String number;

		public Foo(String number) {
			this.number = number;
		}
	}
}
