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

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Helper class for modulo 10/11.
 *
 * @author Hardy Ferentschik
 */
public final class ModUtil {
	private ModUtil() {
	}
	
	/**
	 * Calculate Mod11
	 *
	 * @param digits the digits for which to calculate the checksum
	 * @param multiplier multiplier for the modulo algorithm
	 *
	 * @return the result of the mod11 calculation
	 */
	public static int mod11(final List<Integer> digits, final int multiplier) {
		int sum = 0;
		int weight = 2;

		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			sum += digits.get( index ) * weight++;
			if ( weight > multiplier ) {
				weight = 2;
			}
		}
		return 11 - ( sum % 11 );
	}

	/**
	 * Calculate Mod10 (Luhn) algorithm implementation
	 *
	 * @param digits the digits for which to calculate the checksum including check digit;
	 * @param multiplier multiplier for the modulo algorithm
	 *
	 * @return the result of the mod10 calculation
	 */
	public static int mod10(final List<Integer> digits, final int multiplier) {
		int sum = 0;
		boolean even = false;
		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			int digit = digits.get( index );
			if ( even ) {
				digit *= multiplier;
			}
			if ( digit > 9 ) {
				digit = digit / 10 + digit % 10;
			}
			sum += digit;
			even = !even;
		}
		return sum % 10;
	}
	
	/**
	 * Calculate Mod10 (Luhn)
	 *
	 * @param digits the digits for which to calculate the checksum;
	 * @param checkDigit the check digit
	 * @param multiplier multiplier for the modulo algorithm
	 *
	 * @return the result of the mod10 calculation
	 */
	public static int mod10(final List<Integer> digits, int checkDigit, final int multiplier) {
		List<Integer> digitsIncludingCheckDigit = newArrayList( digits );
		digitsIncludingCheckDigit.add( checkDigit );
		return mod10( digitsIncludingCheckDigit, multiplier );
	}
}


