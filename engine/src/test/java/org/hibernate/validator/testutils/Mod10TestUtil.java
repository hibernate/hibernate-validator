/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutils;

/**
 * Independent reference for the original substring/filter/int-accumulator weighted modulo 10 implementation.
 * Does not call production checksum or digit utilities.
 */
public final class Mod10TestUtil {
	private Mod10TestUtil() {
	}

	public static boolean isValidLegacy(CharSequence value, int start, int end, int checkIndex, boolean ignoreNonDigits, int multiplier, int weight) {
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
		int sum = 0;
		boolean useMultiplier = true;
		for ( int i = body.length() - 1; i >= 0; i-- ) {
			char character = body.charAt( i );
			if ( !Character.isDigit( character ) ) {
				return false;
			}
			int digit = Character.digit( character, 10 );
			digit *= useMultiplier ? multiplier : weight;
			sum += digit;
			useMultiplier = !useMultiplier;
		}
		return Character.isDigit( checkDigit ) && Character.digit( checkDigit, 10 ) == ( 10 - sum % 10 ) % 10;
	}

}
