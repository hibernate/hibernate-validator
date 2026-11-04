/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.Mod10CheckDef;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod10CheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.LuhnTestUtil.RepeatedDigits;
import org.hibernate.validator.testutils.Mod10TestUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for the {@code Mod10CheckValidator}.
 *
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class Mod10CheckValidatorTest {

	@Test
	public void testInvalidStartIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( -1, Integer.MAX_VALUE, -1, false );
		assertThatThrownBy( () -> validator.initialize( modCheck ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testInvalidEndIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, -1, -1, false );
		assertThatThrownBy( () -> validator.initialize( modCheck ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testEndIndexLessThanStartIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 5, 0, -1, false );
		assertThatThrownBy( () -> validator.initialize( modCheck ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testInvalidCheckDigitIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, 10, 5, false );
		assertThatThrownBy( () -> validator.initialize( modCheck ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "A79927398712" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "A79927398712" ), null ) );
	}

	@Test
	public void testValidMod10() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "79927398712", null ) );
	}

	@Test
	public void testValidMod10WithGivenRange() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 13, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "xxx-7992739871-x", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndExplicitCheckDigit() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 13, 15, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "xxx-799273987-x1x", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndCheckDigitIndex() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 12, 13, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-2-456", null ) );
	}

	@Test
	public void testInvalidMod10() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "79927398713" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_GTIN_13() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "4 007630 00011 6" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "1 234567 89012 8" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_GTIN_14() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "0 40 07630 00011 6" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "3 07 12345 00001 0" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_14WithEAN_128() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 5, 22, -1, true );
		validator.initialize( modCheck );
		assertTrue( validator.isValid( new MyCustomStringImpl( "(01) 1 23 45678 90123 1" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidISBN_13() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "978-85-61411-03-9" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "978-1-4302-1957-6" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidIdentcode() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true, 4, 9 );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "56.310 243.031 3" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-812")
	public void testProgrammaticMod11Constraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Product.class )
				.field( "productNumber" )
				.constraint(
						new Mod10CheckDef()
								.multiplier( 3 )
								.weight( 1 )
								.startIndex( 0 )
								.endIndex( 12 )
								.checkDigitIndex( -1 )
								.ignoreNonDigitCharacters( true )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "P-79927398712" );

		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testCheckDigitAtEndIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		validator.initialize( createMod10CheckAnnotation( 0, 10, 10, false ) );

		assertTrue( validator.isValid( "79927398712", null ) );
		assertFalse( validator.isValid( "79927398714", null ) );
	}

	@Test
	public void testNegativeMultiplier() {
		assertThatThrownBy( () -> {
			new Mod10CheckValidator().initialize( createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false, -1, 1 ) );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeWeight() {
		assertThatThrownBy( () -> {
			new Mod10CheckValidator().initialize( createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false, 3, -1 ) );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	public static Object[][] rawRanges() {
		return new Object[][] {
				{ 0, Integer.MAX_VALUE, -1 },
				{ 4, Integer.MAX_VALUE, -1 },
				{ 100, Integer.MAX_VALUE, -1 },
				{ 0, Integer.MAX_VALUE, 0 },
				{ 3, 15, -1 },
				{ 3, 13, 15 },
				{ 0, 10, 10 },
				{ 1, 1, 1 },
				{ 1, 1, 0 },
				{ 2, 8, 0 },
				{ 0, 100, -1 },
				{ 0, Integer.MAX_VALUE, -2 },
				{ 0, 2, -2 },
				{ 0, Integer.MAX_VALUE, Integer.MAX_VALUE },
				{ 0, Integer.MAX_VALUE - 1, -1 }
		};
	}

	@ParameterizedTest
	@MethodSource("rawRanges")
	public void testRawRangesMatchIndependentLegacyReference(int start, int end, int checkIndex) {
		Random random = new Random( 813 );
		String characters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( int[] weights : new int[][] {
				{ 3, 1 },
				{ 0, 0 },
				{ 0, 1 },
				{ 4, 9 },
				{ Integer.MAX_VALUE, Integer.MAX_VALUE },
				{ Integer.MAX_VALUE, 1 },
				{ 2, Integer.MAX_VALUE } } ) {
			for ( boolean ignore : new boolean[] { false, true } ) {
				Mod10CheckValidator validator = new Mod10CheckValidator();
				validator.initialize( createMod10CheckAnnotation( start, end, checkIndex, ignore, weights[0], weights[1] ) );
				for ( CharSequence value : new CharSequence[] {
						null,
						"",
						"0",
						"٠",
						"1",
						"79927398712",
						"7992739871٢",
						"٧٩٩٢٧٣٩٨٧١٠",
						"abc0",
						" -0",
						"79927398712-",
						"123-7992739871-2-456",
						new StringBuilder( "998" ) } ) {
					assertEquals( Mod10TestUtil.isValidLegacy( value, start, end, checkIndex, ignore, weights[0], weights[1] ), validator.isValid( value, null ) );
				}
				for ( int sample = 0; sample < 50; sample++ ) {
					StringBuilder value = new StringBuilder();
					int length = random.nextInt( 30 );
					for ( int i = 0; i < length; i++ ) {
						value.append( sample % 3 == 0
								? (char) ( '0' + random.nextInt( 10 ) )
								: characters.charAt( random.nextInt( characters.length() ) ) );
					}
					assertEquals( Mod10TestUtil.isValidLegacy( value, start, end, checkIndex, ignore, weights[0], weights[1] ), validator.isValid( value, null ), value.toString() );
				}
			}
		}
	}

	@Test
	public void testOverflowVectors() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		validator.initialize( createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false, Integer.MAX_VALUE, Integer.MAX_VALUE ) );
		// Both multiplication and addition retain the old integer wraparound.
		assertTrue( validator.isValid( "91", null ) );
		assertTrue( validator.isValid( "998", null ) );
		assertTrue( validator.isValid( "112", null ) );
		assertFalse( validator.isValid( "994", null ) );
	}

	@Test
	public void testCharacterSequenceDoesNotRequireStringConversion() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		validator.initialize( createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false ) );
		assertTrue( validator.isValid( new RepeatedDigits( 1, '1', '7' ), null ) );
	}

	private Mod10Check createMod10CheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits, int multiplier, int weight) {
		ConstraintAnnotationDescriptor.Builder<Mod10Check> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Mod10Check.class );
		descriptorBuilder.setAttribute( "startIndex", start );
		descriptorBuilder.setAttribute( "endIndex", end );
		descriptorBuilder.setAttribute( "checkDigitIndex", checkDigitIndex );
		descriptorBuilder.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigits );
		descriptorBuilder.setAttribute( "multiplier", multiplier );
		descriptorBuilder.setAttribute( "weight", weight );

		return descriptorBuilder.build().getAnnotation();
	}

	private Mod10Check createMod10CheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits) {
		return this.createMod10CheckAnnotation( start, end, checkDigitIndex, ignoreNonDigits, 3, 1 );
	}

	private static class Product {
		private final String productNumber;

		private Product(String productNumber) {
			this.productNumber = productNumber;
		}
	}
}
