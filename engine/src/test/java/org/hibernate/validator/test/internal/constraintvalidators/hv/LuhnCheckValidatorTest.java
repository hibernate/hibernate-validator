/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Random;

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutils.LuhnTestUtil;
import org.hibernate.validator.testutils.LuhnTestUtil.RepeatedDigits;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for the {@code LuhnCheckValidator}.
 *
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class LuhnCheckValidatorTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidStartIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( -1, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidEndIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, -1, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEndIndexLessThanStartIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 5, 0, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCheckDigitIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, 10, 5, false );
		validator.initialize( modCheck );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testValidMod10() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "79927398713", null ) );
	}

	@Test
	public void testValidMod10WithGivenRange() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 3, 15, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-3", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndCheckDigitIndex() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 3, 13, 15, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-3-456", null ) );
	}

	@Test
	public void testInvalidMod10() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "79927398714" ), null ) );
	}

	@Test
	public void testCheckDigitAtEndIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		validator.initialize( createLuhnCheckAnnotation( 0, 10, 10, false ) );

		assertTrue( validator.isValid( "79927398713", null ) );
		assertFalse( validator.isValid( "79927398714", null ) );
	}

	@DataProvider
	public Object[][] rawRanges() {
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

	@Test(dataProvider = "rawRanges")
	public void testRawRangesMatchIndependentLegacyReference(int start, int end, int checkIndex) {
		Random random = new Random( 906 );
		String characters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( boolean ignore : new boolean[] { false, true } ) {
			LuhnCheckValidator validator = new LuhnCheckValidator();
			validator.initialize( createLuhnCheckAnnotation( start, end, checkIndex, ignore ) );
			for ( CharSequence value : new CharSequence[] {
					null,
					"",
					"0",
					"٠",
					"1",
					"79927398713",
					"7992739871٣",
					"٧٩٩٢٧٣٩٨٧١٠",
					"abc0",
					" -0",
					"79927398713-",
					"123-7992739871-3-456",
					new StringBuilder( "18" ) } ) {
				assertEquals( validator.isValid( value, null ), LuhnTestUtil.isValidLegacy( value, start, end, checkIndex, ignore ) );
			}
			for ( int sample = 0; sample < 100; sample++ ) {
				StringBuilder value = new StringBuilder();
				int length = random.nextInt( 30 );
				for ( int i = 0; i < length; i++ ) {
					value.append( sample % 3 == 0
							? (char) ( '0' + random.nextInt( 10 ) )
							: characters.charAt( random.nextInt( characters.length() ) ) );
				}
				assertEquals( validator.isValid( value, null ), LuhnTestUtil.isValidLegacy( value, start, end, checkIndex, ignore ), value.toString() );
			}
		}
	}

	@Test
	public void testCharacterSequenceDoesNotRequireStringConversion() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		validator.initialize( createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false ) );
		assertTrue( validator.isValid( new RepeatedDigits( 1, '1', '8' ), null ) );
	}

	private LuhnCheck createLuhnCheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits) {
		ConstraintAnnotationDescriptor.Builder<LuhnCheck> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( LuhnCheck.class );
		descriptorBuilder.setAttribute( "startIndex", start );
		descriptorBuilder.setAttribute( "endIndex", end );
		descriptorBuilder.setAttribute( "checkDigitIndex", checkDigitIndex );
		descriptorBuilder.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigits );

		return descriptorBuilder.build().getAnnotation();
	}
}
