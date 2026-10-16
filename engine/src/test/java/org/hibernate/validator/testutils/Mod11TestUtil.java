/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Independent copies of the original modulo 11 arithmetic and input handling.
 * Does not call production checksum or digit utilities.
 */
public final class Mod11TestUtil {
	private Mod11TestUtil() {
	}

	public static int calculateMod11CheckLegacy(List<Integer> digits, int threshold) {
		int sum = 0;
		int multiplier = 2;
		for ( int i = digits.size() - 1; i >= 0; i-- ) {
			sum += digits.get( i ) * multiplier++;
			if ( multiplier > threshold ) {
				multiplier = 2;
			}
		}
		return 11 - sum % 11;
	}

	public static int calculateModXCheckWithWeightsLegacy(List<Integer> digits, int modulus, int threshold, int... weights) {
		int sum = 0;
		int multiplier = 1;
		for ( int i = digits.size() - 1; i >= 0; i-- ) {
			if ( weights.length != 0 ) {
				multiplier = weights[weights.length - i % weights.length - 1];
			}
			else {
				multiplier++;
				if ( multiplier > threshold ) {
					multiplier = 2;
				}
			}
			sum += digits.get( i ) * multiplier;
		}
		return modulus - sum % modulus;
	}

	public static boolean isValidLegacy(CharSequence value, int start, int end, int checkIndex, boolean ignoreNonDigits,
			int threshold, char treat10As, char treat11As, boolean leftToRight, int... weights) {
		if ( value == null ) {
			return true;
		}
		String original = value.toString();
		String body;
		char checkDigit;
		try {
			if ( end == Integer.MAX_VALUE ) {
				body = original.substring( 0, original.length() - 1 );
			}
			else if ( checkIndex == -1 || checkIndex == end ) {
				body = original.substring( start, end );
			}
			else {
				body = original.substring( start, end + 1 );
			}
			checkDigit = original.charAt( checkIndex == -1 ? ( end == Integer.MAX_VALUE ? original.length() - 1 : end ) : checkIndex );
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
		if ( ignoreNonDigits ) {
			body = body.replaceAll( "[^0-9]", "" );
		}
		List<Integer> digits = new ArrayList<>();
		for ( int i = 0; i < body.length(); i++ ) {
			char character = body.charAt( i );
			if ( !Character.isDigit( character ) ) {
				return false;
			}
			digits.add( Character.digit( character, 10 ) );
		}
		if ( leftToRight ) {
			Collections.reverse( digits );
		}
		int complement = calculateModXCheckWithWeightsLegacy( digits, 11, threshold, weights );
		if ( complement == 10 ) {
			return checkDigit == treat10As;
		}
		if ( complement == 11 ) {
			return checkDigit == treat11As;
		}
		return Character.isDigit( checkDigit ) && Character.digit( checkDigit, 10 ) == complement;
	}
}
