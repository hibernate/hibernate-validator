/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
}
