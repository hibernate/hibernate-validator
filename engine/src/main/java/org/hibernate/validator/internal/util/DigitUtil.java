/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

/**
 * Character conversion policies used by checksum validators.
 */
public final class DigitUtil {
	private DigitUtil() {
	}

	/**
	 * @return the ASCII decimal digit, or {@code -1} for any other character
	 */
	public static int getAsciiDigit(char character) {
		return character >= '0' && character <= '9' ? character - '0' : -1;
	}

	/**
	 * @return the Unicode decimal digit represented by this UTF-16 character, or {@code -1}
	 */
	public static int getDecimalDigit(char character) {
		return Character.isDigit( character ) ? Character.digit( character, 10 ) : -1;
	}
}
