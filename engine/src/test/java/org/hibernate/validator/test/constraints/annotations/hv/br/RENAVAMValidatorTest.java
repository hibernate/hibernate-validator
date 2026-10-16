/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.RENAVAM;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.LuhnTestUtil.RepeatedDigits;
import org.hibernate.validator.testutils.Mod11TestUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Matheus Pereira
 */
public class RENAVAMValidatorTest extends AbstractConstrainedTest {

	private static final String[] VALID_CURRENT_RENAVAMS = {
			"01012655145",
			"00600052230",
			"11616434907",
			"66408436045",
			"60262340350",
			"03833615348",
			"72851624445",
			"83682910859",
			"32842105741",
			"02163351640"
	};

	private static final String[] VALID_LEGACY_RENAVAMS = {
			"137723016",
			"616425929",
			"123456789",
			"987654322",
			"264084365",
			"602623405",
			"383361532",
			"728516241",
			"836829107",
			"328421057",
			"216335167"
	};

	private static final String[] INVALID_CHECK_DIGIT_CURRENT_RENAVAMS = {
			"01012655146",
			"00600052231",
			"11616434908",
			"66408436046",
			"60262340351",
			"03833615349",
			"72851624446",
			"83682910850",
			"32842105742",
			"02163351641"
	};

	private static final String[] INVALID_CHECK_DIGIT_LEGACY_RENAVAMS = {
			"137723017",
			"616425920",
			"123456780",
			"987654323",
			"264084366",
			"602623406",
			"383361533",
			"728516242",
			"836829108",
			"328421058",
			"216335168"
	};

	private static final String[] INVALID_LENGTH_RENAVAMS = {
			"12345678",
			"1234567890",
			"123456789012"
	};

	private static final String[] INVALID_CHARACTER_RENAVAMS = {
			"010126551A5",
			"1377230A6",
			"01012655\u066145"
	};

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testCurrentRenavamRepresentationIsValid() {
		for ( String renavam : VALID_CURRENT_RENAVAMS ) {
			assertNoViolations( validator.validate( new Vehicle( renavam ) ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testLegacyRenavamRepresentationIsValid() {
		for ( String renavam : VALID_LEGACY_RENAVAMS ) {
			assertNoViolations( validator.validate( new Vehicle( renavam ) ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testNullIsValid() {
		assertNoViolations( validator.validate( new Vehicle( null ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testCurrentRenavamWithInvalidCheckDigitIsInvalid() {
		for ( String renavam : INVALID_CHECK_DIGIT_CURRENT_RENAVAMS ) {
			Set<ConstraintViolation<Vehicle>> violations = validator.validate( new Vehicle( renavam ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( RENAVAM.class ).withProperty( "renavam" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testLegacyRenavamWithInvalidCheckDigitIsInvalid() {
		for ( String renavam : INVALID_CHECK_DIGIT_LEGACY_RENAVAMS ) {
			Set<ConstraintViolation<Vehicle>> violations = validator.validate( new Vehicle( renavam ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( RENAVAM.class ).withProperty( "renavam" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testUnsupportedLengthIsInvalid() {
		for ( String renavam : INVALID_LENGTH_RENAVAMS ) {
			Set<ConstraintViolation<Vehicle>> violations = validator.validate( new Vehicle( renavam ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( RENAVAM.class ).withProperty( "renavam" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2260")
	public void testNonDigitCharacterIsInvalid() {
		for ( String renavam : INVALID_CHARACTER_RENAVAMS ) {
			Set<ConstraintViolation<Vehicle>> violations = validator.validate( new Vehicle( renavam ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( RENAVAM.class ).withProperty( "renavam" )
			);
		}
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Vehicle.class )
				.getConstraintsForProperty( "renavam" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "renavamBoundaries")
	public void testRenavamBoundaries(CharSequence renavam, boolean valid) {
		assertEquals( validator.validate( new Vehicle( renavam ) ).isEmpty(), valid );
	}

	@DataProvider
	public Object[][] renavamBoundaries() {
		return new Object[][] {
				{ "", false },
				{ "000000000", true },
				{ "00000000000", true },
				// Remainders zero and one both produce a zero check digit in both representations.
				{ "000000140", true },
				{ "000000060", true },
				{ "00000000140", true },
				{ "00000000060", true },
				{ "00000000141", false },
				{ "000000061", false },
				{ "00000000٠", false },
				{ "０00000000", false },
				{ "0000-0000", false },
				{ "00000000 ", false },
				{ " 00000000", false },
				{ "00000000\n", false },
				{ "0000000000\n", false },
				{ new StringBuilder( "137723016" ), true },
				{ new StringBuilder( "01012655145" ), true },
				{ "0".repeat( 100_000 ), false }
		};
	}

	@Test
	public void testMatchesIndependentLegacyMod11Reference() {
		Pattern legacyFormat = Pattern.compile( "(?:[0-9]{9}|[0-9]{11})" );
		Random random = new Random( 2260 );
		String mixedCharacters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( int size : new int[] { 9, 11 } ) {
			for ( int sample = 0; sample < 300; sample++ ) {
				StringBuilder renavam = new StringBuilder();
				for ( int i = 0; i < size; i++ ) {
					renavam.append( sample % 2 == 0
							? (char) ( '0' + random.nextInt( 10 ) )
							: mixedCharacters.charAt( random.nextInt( mixedCharacters.length() ) ) );
				}
				assertEquals( validator.validate( new Vehicle( renavam ) ).isEmpty(),
						legacyFormat.matcher( renavam ).matches() && Mod11TestUtil.isValidLegacy( renavam, 0, Integer.MAX_VALUE, -1, false, 9, '0', '0', false ), renavam.toString() );
			}
		}
	}

	@Test
	public void testCharacterSequenceDoesNotRequireStringConversion() {
		assertNoViolations( validator.validate( new Vehicle( new RepeatedDigits( 8, '0', '0' ) ) ) );
		assertNoViolations( validator.validate( new Vehicle( new RepeatedDigits( 10, '0', '0' ) ) ) );
	}

	public static class Vehicle {

		@RENAVAM
		private CharSequence renavam;

		public Vehicle(CharSequence renavam) {
			this.renavam = renavam;
		}
	}
}
