/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.Mod11TestUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for the {@code ModUtil}.
 *
 * @author Hardy Ferentschik
 */
public class ModUtilTest {

	@Test
	public void testPassLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 5 );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 3 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 9 );
	}

	@DataProvider
	public Object[][] luhnRanges() {
		return new Object[][] {
				{ "7992739871", 0, 10, false, 67L },
				{ "xx7992739871yy", 2, 12, false, 67L },
				{ "٧٩٩٢٧٣٩٨٧١", 0, 10, false, 67L },
				{ "٧٩٩٢٧٣٩٨٧١", 0, 10, true, 0L },
				{ "a1٢b", 0, 4, false, -1L },
				{ "a1٢b", 0, 4, true, 2L },
				{ "1-2", 0, 3, true, 5L },
				{ "1-2", 0, 3, false, -1L },
				{ "𝟘", 0, 2, false, -1L },
				{ "𝟘", 0, 2, true, 0L },
				{ "", 0, 0, false, 0L },
				{ "1", 1, 1, false, 0L },
				{ "1", -1, 1, true, -1L },
				{ "1", 0, 2, true, -1L },
				{ "1", 1, 0, true, -1L }
		};
	}

	@Test(dataProvider = "luhnRanges")
	public void testCharacterSequenceLuhnSum(String value, int start, int end, boolean ignoreNonDigits, long expected) {
		assertEquals( ModUtil.calculateLuhnSum( value, start, end, ignoreNonDigits ), expected );
	}

	@Test
	public void testFailLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 2 );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 4 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 0 );
	}

	@DataProvider
	public Object[][] mod10Ranges() {
		return new Object[][] {
				{ "7992739871", 0, 10, false, 3, 1, 2 },
				{ "xx7992739871yy", 2, 12, false, 3, 1, 2 },
				{ "١٢", 0, 2, false, 3, 1, 3 },
				{ "١٢", 0, 2, true, 3, 1, 0 },
				{ "1٢", 0, 2, true, 3, 1, 7 },
				{ "1-2", 0, 3, true, 3, 1, 3 },
				{ "1-2", 0, 3, false, 3, 1, -1 },
				{ "𝟘", 0, 2, false, 3, 1, -1 },
				{ "𝟘", 0, 2, true, 3, 1, 0 },
				{ "", 0, 0, false, 3, 1, 0 },
				{ "1", 1, 1, false, 3, 1, 0 },
				{ "1", -1, 1, true, 3, 1, -1 },
				{ "1", 0, 2, true, 3, 1, -1 },
				{ "1", 1, 0, true, 3, 1, -1 },
				{ "12", 0, 2, false, 0, 0, 0 },
				{ "12", 0, 2, false, 0, 1, 9 },
				{ "9", 0, 1, false, Integer.MAX_VALUE, 1, 1 },
				{ "99", 0, 2, false, Integer.MAX_VALUE, Integer.MAX_VALUE, 8 },
				{ "11", 0, 2, false, Integer.MAX_VALUE, Integer.MAX_VALUE, 2 }
		};
	}

	@Test(dataProvider = "mod10Ranges")
	public void testCharacterSequenceMod10Check(String value, int start, int end, boolean ignoreNonDigits, int multiplier, int weight, int expected) {
		assertEquals( ModUtil.calculateMod10Check( value, start, end, ignoreNonDigits, multiplier, weight ), expected );
	}

	@Test
	public void testListMod10OverflowIsPreserved() {
		assertEquals( ModUtil.calculateMod10Check( Arrays.asList( 9, 9 ), Integer.MAX_VALUE, Integer.MAX_VALUE ), 8 );
		assertEquals( ModUtil.calculateMod10Check( Arrays.asList( 1, 1 ), Integer.MAX_VALUE, Integer.MAX_VALUE ), 2 );
	}

	@Test
	public void testPassMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 7 );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 7 );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 10 );
	}

	@DataProvider
	public Object[][] mod11Thresholds() {
		return new Object[][] { { Integer.MIN_VALUE }, { -1 }, { 0 }, { 1 }, { 2 }, { 3 }, { 9 }, { Integer.MAX_VALUE } };
	}

	@Test(dataProvider = "mod11Thresholds")
	public void testMod11PathsMatchIndependentLegacyReference(int threshold) {
		Random random = new Random( 813 );
		for ( int length = 0; length < 32; length++ ) {
			List<Integer> digits = new ArrayList<>();
			StringBuilder value = new StringBuilder( "xx" );
			for ( int i = 0; i < length; i++ ) {
				int digit = random.nextInt( 10 );
				digits.add( digit );
				value.append( digit );
			}
			value.append( "yy" );
			int expected = Mod11TestUtil.calculateMod11CheckLegacy( digits, threshold );
			assertEquals( ModUtil.calculateMod11Check( digits, threshold ), expected );
			assertEquals( ModUtil.calculateMod11Check( value, 2, 2 + length, threshold ), expected );
			for ( int[] weights : new int[][] { { }, { 2 }, { 2, 7, 3 }, { -1, 0, 3 }, { Integer.MAX_VALUE, Integer.MIN_VALUE } } ) {
				for ( int modulus : new int[] { 10, 11, -11, Integer.MIN_VALUE, Integer.MAX_VALUE } ) {
					assertEquals( ModUtil.calculateModXCheckWithWeights( digits, modulus, threshold, weights ),
							Mod11TestUtil.calculateModXCheckWithWeightsLegacy( digits, modulus, threshold, weights ) );
					List<Integer> reversed = new ArrayList<>( digits );
					Collections.reverse( reversed );
					assertEquals( ModUtil.calculateModXCheckWithWeights( reversed, modulus, threshold, weights ),
							Mod11TestUtil.calculateModXCheckWithWeightsLegacy( reversed, modulus, threshold, weights ) );
				}
			}
		}
	}

	@Test
	public void testMod11RangesAndExceptionalResults() {
		assertEquals( ModUtil.calculateMod11Check( "123", 0, 3, 1 ), 10 );
		assertEquals( ModUtil.calculateMod11Check( "123", 0, 3, 2 ), 10 );
		assertEquals( ModUtil.calculateMod11Check( "123", 0, 3, 3 ), 8 );
		assertEquals( ModUtil.calculateMod11Check( "", 0, 0, 9 ), 11 );
		assertEquals( ModUtil.calculateMod11Check( "123", -1, 2, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( "123", 2, 1, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( "123", 0, 4, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( "1-2", 0, 3, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( "١٢", 0, 2, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( "９", 0, 1, 9 ), -1 );
		assertEquals( ModUtil.calculateMod11Check( 0 ), 11 );
		assertEquals( ModUtil.calculateMod11Check( 1 ), 10 );
		assertEquals( ModUtil.calculateMod11Check( -1 ), 12 );
		assertEquals( ModUtil.calculateMod11Check( Integer.MIN_VALUE ), 13 );
		for ( int[] example : new int[][] { { -1, -1 }, { 1, 1 }, { 9, 9 }, { 10, 0 }, { 11, 0 }, { 12, 12 }, { 21, 21 } } ) {
			assertEquals( ModUtil.toZeroMappedMod11CheckDigit( example[0] ), example[1] );
		}
	}

	@Test
	public void testMod11IntegerOverflowDoesNotCollideWithInvalidSentinel() {
		// Mathematical sum2812837500 wraps to -1482129796, whose signed modulo11 complement is15.
		String value = "9".repeat( 25_000 );
		List<Integer> digits = Collections.nCopies( 25_000, 9 );
		assertEquals( Mod11TestUtil.calculateMod11CheckLegacy( digits, Integer.MAX_VALUE ), 15 );
		assertEquals( ModUtil.calculateMod11Check( digits, Integer.MAX_VALUE ), 15 );
		assertEquals( ModUtil.calculateMod11Check( value, 0, value.length(), Integer.MAX_VALUE ), 15 );
	}

	@Test
	public void testCustomWeightsRemainAnchoredToOriginalIndices() {
		assertEquals( ModUtil.calculateModXCheckWithWeights( Arrays.asList( 1, 2 ), 11, 9, 2, 7, 3 ), 5 );
		assertEquals( ModUtil.calculateModXCheckWithWeights( Arrays.asList( 2, 1 ), 11, 9, 2, 7, 3 ), 9 );
		assertEquals( ModUtil.calculateModXCheckWithWeights( Arrays.asList( 1, 2, 3 ), 11, 9, 2, 7 ), 1 );
	}

	@Test(expectedExceptions = ArithmeticException.class)
	public void testZeroModulusStillThrows() {
		ModUtil.calculateModXCheckWithWeights( Arrays.asList( 1, 2 ), 0, 9 );
	}

	@Test
	public void testFailMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 6 );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 9 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testFailMod11SelfValidation() throws Exception {
		List<Integer> digits = Arrays.asList( 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );
		assertFalse( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) == 1 );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 11 );
		assertEquals( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ), 11 );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );
		assertFalse( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) == 1 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 5 );
		assertFalse( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) == 5 );

		digits = Arrays.asList( 3, 7, 8, 7, 9, 6, 9, 5, 0, 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 2 );
		assertFalse( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) == 2 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6, 5 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 2 );
		assertFalse( ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) == 2 );
	}

}
