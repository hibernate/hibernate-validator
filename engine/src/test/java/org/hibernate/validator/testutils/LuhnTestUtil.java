/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutils;

/**
 * Independent reference for the original substring/filter/int-accumulator Luhn implementation.
 * Does not call production checksum or digit utilities.
 */
public final class LuhnTestUtil {
	private LuhnTestUtil() {
	}

	public static boolean isValidLegacy(CharSequence value, int start, int end, int checkIndex, boolean ignoreNonDigits) {
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
		boolean doubleDigit = true;
		for ( int i = body.length() - 1; i >= 0; i-- ) {
			char character = body.charAt( i );
			if ( !Character.isDigit( character ) ) {
				return false;
			}
			int digit = Character.digit( character, 10 );
			if ( doubleDigit ) {
				digit *= 2;
				if ( digit > 9 ) {
					digit -= 9;
				}
			}
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return Character.isDigit( checkDigit ) && Character.digit( checkDigit, 10 ) == ( 10 - sum % 10 ) % 10;
	}

	/**
	 * Virtual input that avoids allocating a string and rejects attempts to normalize it.
	 */
	public static final class RepeatedDigits implements CharSequence {
		private final int bodyLength;
		private final char digit;
		private final char checkDigit;

		public RepeatedDigits(int bodyLength, char digit, char checkDigit) {
			this.bodyLength = bodyLength;
			this.digit = digit;
			this.checkDigit = checkDigit;
		}

		@Override
		public int length() {
			return bodyLength + 1;
		}

		@Override
		public char charAt(int index) {
			if ( index < 0 || index > bodyLength ) {
				throw new IndexOutOfBoundsException();
			}
			return index == bodyLength ? checkDigit : digit;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new AssertionError( "Checksum validation must not extract subsequences" );
		}

		@Override
		public String toString() {
			throw new AssertionError( "Checksum validation must not normalize the input to a string" );
		}
	}
}
