/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
			int digit = digits.get( index );

			if ( even ) {
				digit <<= 1;
			}
			if ( digit > 9 ) {
				digit -= 9;
			}
			sum += digit;
			even = !even;
		}
		return ( 10 - ( sum % 10 ) ) % 10;
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
			int digit = digits.get( index );

			if ( even ) {
				digit *= multiplier;
			}
			else {
				digit *= weight;
			}

			sum += digit;
			even = !even;
		}
		return ( 10 - ( sum % 10 ) ) % 10;
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
			sum += digits.get( index ) * multiplier++;
			if ( multiplier > threshold ) {
				multiplier = 2;
			}
		}
		return 11 - ( sum % 11 );
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
				multiplier++;
				if ( multiplier > threshold ) {
					multiplier = 2;
				}
			}
			sum += digits.get( index ) * multiplier;
		}
		return moduloParam - ( sum % moduloParam );
	}
}
