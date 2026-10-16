/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

import java.util.List;

/**
 * Helper class for modulo 10/11.
 *
 * @author Hardy Ferentschik
 */
public final class ModUtil {
	private ModUtil() {
	}

	/**
	 * Calculate Luhn Modulo 10 checksum (Luhn algorithm implementation)
	 *
	 * @param digits The digits over which to calculate the checksum
	 *
	 * @return the result of the mod10 checksum calculation
	 */
	public static int calculateLuhnMod10Check(final List<Integer> digits) {
		int sum = 0;
		boolean even = true;
		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			sum += luhnDigit( digits.get( index ), even );
			even = !even;
		}
		return ( 10 - ( sum % 10 ) ) % 10;
	}

	/**
	 * Calculates a Luhn sum over a raw character range, with its rightmost digit doubled.
	 * Filtering does not consume a position in the alternating digit weights.
	 *
	 * @param value the input character sequence
	 * @param start the inclusive raw start index
	 * @param end the exclusive raw end index
	 * @param ignoreNonDigits whether to skip all characters other than ASCII digits; otherwise Unicode decimal digits are required
	 * @return the sum, or {@code -1} for an invalid range or an invalid digit in strict mode
	 */
	public static long calculateLuhnSum(CharSequence value, int start, int end, boolean ignoreNonDigits) {
		if ( start < 0 || end < start || end > value.length() ) {
			return -1;
		}

		// At most nine per input character fits in a long for every possible CharSequence length.
		long sum = 0;
		boolean doubleDigit = true;
		for ( int i = end - 1; i >= start; i-- ) {
			char character = value.charAt( i );
			int digit;
			if ( ignoreNonDigits ) {
				digit = DigitUtil.getAsciiDigit( character );
				if ( digit < 0 ) {
					continue;
				}
			}
			else {
				digit = DigitUtil.getDecimalDigit( character );
				if ( digit < 0 ) {
					return -1;
				}
			}
			sum += luhnDigit( digit, doubleDigit );
			doubleDigit = !doubleDigit;
		}
		return sum;
	}

	private static int luhnDigit(int digit, boolean doubleDigit) {
		if ( doubleDigit ) {
			digit <<= 1;
		}
		return digit > 9 ? digit - 9 : digit;
	}

	/**
	 * Calculate Generic Modulo 10 checksum
	 *
	 * @param digits The digits over which to calculate the checksum
	 * @param multiplier Multiplier used for the odd digits in the algorithm
	 * @param weight Multiplier used for the even digits in the algorithm
	 *
	 * @return the result of the mod10 checksum calculation
	 */
	public static int calculateMod10Check(final List<Integer> digits, int multiplier, int weight) {
		int sum = 0;
		boolean even = true;
		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			sum += digits.get( index ) * ( even ? multiplier : weight );
			even = !even;
		}
		return ( 10 - ( sum % 10 ) ) % 10;
	}

	/**
	 * Calculates a weighted modulo 10 check digit over a raw character range.
	 * The rightmost retained digit uses {@code multiplier}; skipped characters do not advance the alternating weights.
	 * Integer multiplication and accumulation preserve the overflow behavior of the list-based method.
	 *
	 * @param value the input character sequence
	 * @param start the inclusive raw start index
	 * @param end the exclusive raw end index
	 * @param ignoreNonDigits whether to skip all characters other than ASCII digits; otherwise Unicode decimal digits are required
	 * @param multiplier the weight of the rightmost retained digit and alternating digits
	 * @param weight the weight of the other digits
	 * @return the check digit, or {@code -1} for an invalid range or an invalid digit in strict mode
	 */
	public static int calculateMod10Check(CharSequence value, int start, int end, boolean ignoreNonDigits, int multiplier, int weight) {
		if ( start < 0 || end < start || end > value.length() ) {
			return -1;
		}

		int sum = 0;
		boolean useMultiplier = true;
		for ( int i = end - 1; i >= start; i-- ) {
			char character = value.charAt( i );
			int digit;
			if ( ignoreNonDigits ) {
				digit = DigitUtil.getAsciiDigit( character );
				if ( digit < 0 ) {
					continue;
				}
			}
			else {
				digit = DigitUtil.getDecimalDigit( character );
				if ( digit < 0 ) {
					return -1;
				}
			}
			sum += digit * ( useMultiplier ? multiplier : weight );
			useMultiplier = !useMultiplier;
		}
		return ( 10 - sum % 10 ) % 10;
	}

	/**
	 * Calculate Modulo 11 checksum
	 *
	 * @param digits the digits for which to calculate the checksum
	 * @param threshold the threshold for the Mod11 algorithm multiplier growth
	 *
	 * @return the result of the mod11 checksum calculation
	 */
	public static int calculateMod11Check(final List<Integer> digits, final int threshold) {
		int sum = 0;
		int multiplier = 2;

		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			sum += digits.get( index ) * multiplier;
			multiplier = nextMod11Multiplier( multiplier, threshold );
		}
		return calculateMod11Check( sum );
	}

	/**
	 * Calculates the raw modulo 11 complement of a weighted sum, preserving signed integer arithmetic.
	 */
	public static int calculateMod11Check(int weightedSum) {
		return calculateModCheck( weightedSum, 11 );
	}

	/**
	 * Maps raw modulo 11 complements ten and eleven to zero, leaving all other results unchanged.
	 */
	public static int toZeroMappedMod11CheckDigit(int complement) {
		return complement == 10 || complement == 11 ? 0 : complement;
	}

	/**
	 * Calculates a raw modulo 11 complement over ASCII digits, scanning from right to left with weights
	 * cycling from two through {@code threshold}. Multiplication, accumulation and weight increments retain
	 * the integer overflow behavior of the list-based method.
	 *
	 * @param value the input character sequence
	 * @param start the inclusive raw start index
	 * @param end the exclusive raw end index
	 * @param threshold the maximum weight before resetting to two; values below two retain weight two
	 * @return the raw complement (one through twenty-one), or {@code -1} for an invalid range or a non-ASCII digit
	 */
	public static int calculateMod11Check(CharSequence value, int start, int end, int threshold) {
		if ( start < 0 || end < start || end > value.length() ) {
			return -1;
		}
		int sum = 0;
		int multiplier = 2;
		for ( int i = end - 1; i >= start; i-- ) {
			int digit = DigitUtil.getAsciiDigit( value.charAt( i ) );
			if ( digit < 0 ) {
				return -1;
			}
			sum += digit * multiplier;
			multiplier = nextMod11Multiplier( multiplier, threshold );
		}
		return calculateMod11Check( sum );
	}

	/**
	 * Calculate Modulo 11 checksum assuming that the threshold is Integer.MAX_VALUE
	 *
	 * @param digits the digits for which to calculate the checksum
	 *
	 * @return the result of the mod11 checksum calculation
	 */
	public static int calculateMod11Check(final List<Integer> digits) {
		return calculateMod11Check( digits, Integer.MAX_VALUE );
	}

	/**
	 * Calculate Modulo {@code moduloParam} checksum with given weights. If no weights are provided then weights similar to Modulo 11 checksum will be used.
	 * In case when there will be not enough weights provided the ones provided will be used in a looped manner.
	 *
	 * @param digits the digits for which to calculate the checksum
	 * @param moduloParam modulo parameter to be used
	 * @param weights weights for the sum.
	 *
	 * @return the result of mod checksum calculation
	 */
	public static int calculateModXCheckWithWeights(final List<Integer> digits, int moduloParam, final int threshold, int... weights) {
		int sum = 0;
		int multiplier = 1;

		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			if ( weights.length != 0 ) {
				multiplier = weights[weights.length - index % weights.length - 1];
			}
			else {
				multiplier = nextMod11Multiplier( multiplier, threshold );
			}
			sum += digits.get( index ) * multiplier;
		}
		return calculateModCheck( sum, moduloParam );
	}

	private static int nextMod11Multiplier(int multiplier, int threshold) {
		int next = multiplier + 1;
		return next > threshold ? 2 : next;
	}

	private static int calculateModCheck(int weightedSum, int modulus) {
		return modulus - weightedSum % modulus;
	}

}
