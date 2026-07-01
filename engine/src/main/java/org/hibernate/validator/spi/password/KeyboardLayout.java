/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.Incubating;

/**
 * Defines a keyboard layout as a mapping from characters to physical key positions.
 * Used by the {@code noKeyboardWalk()} password policy rule to detect walks across
 * adjacent keys.
 * <p>
 * Pre-built layouts are available as constants. Custom layouts can be created via
 * {@link #of(String[], String[], String[], String[])}, where each array represents
 * a keyboard row (top to bottom) and each string in the array groups all characters
 * produced by the same physical key (e.g. {@code "aAą"} for a key that produces
 * {@code a}, {@code A}, and {@code ą}).
 *
 * @since 9.2.0
 */
@Incubating
public final class KeyboardLayout {

	private static final int KEY_WIDTH = 2;
	private static final int MAX_COL_DIFF = KEY_WIDTH + 1;
	private static final int[] ROW_OFFSETS = { 0, 1, 2, 3 };

	/**
	 * Standard US QWERTY layout.
	 */
	public static final KeyboardLayout QWERTY = of(
			new String[] { "`~", "1!", "2@", "3#", "4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+" },
			new String[] { "qQ", "wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}", "\\|" },
			new String[] { "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'\"" },
			new String[] { "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?" }
	);

	/**
	 * French AZERTY layout, including diacritical characters on dedicated keys.
	 */
	public static final KeyboardLayout AZERTY = of(
			new String[] { "²", "&1", "é2~", "\"3#", "'4{", "(5[", "-6|", "è7`", "_8\\", "ç9^", "à0@", ")°]", "=+}" },
			new String[] { "aA", "zZ", "eE€", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "^¨", "$£¤" },
			new String[] { "qQ", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "mM", "ù%", "*µ" },
			new String[] { "wW", "xX", "cC", "vV", "bB", "nN", ",?", ";.", ":/", "!§" }
	);

	/**
	 * Dvorak simplified layout.
	 */
	public static final KeyboardLayout DVORAK = of(
			new String[] { "`~", "1!", "2@", "3#", "4$", "5%", "6^", "7&", "8*", "9(", "0)", "[{", "]}" },
			new String[] { "'\"", ",<", ".>", "pP", "yY", "fF", "gG", "cC", "rR", "lL", "/?", "=+", "\\|" },
			new String[] { "aA", "oO", "eE", "uU", "iI", "dD", "hH", "tT", "nN", "sS", "-_" },
			new String[] { ";:", "qQ", "jJ", "kK", "xX", "bB", "mM", "wW", "vV", "zZ" }
	);

	/**
	 * Nordic QWERTY layout (Swedish/Finnish variant) with Å, Ä, Ö keys.
	 */
	public static final KeyboardLayout NORDIC = of(
			new String[] { "§½", "1!", "2\"@", "3#£", "4¤$", "5%", "6&", "7/{", "8([", "9)]", "0=}", "+?\\", "´`" },
			new String[] { "qQ", "wW", "eE€", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "åÅ", "¨^~" },
			new String[] { "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", "öÖ", "äÄ", "'*" },
			new String[] { "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",;", ".:", "-_" }
	);

	/**
	 * Ukrainian Cyrillic (ЙЦУКЕН) layout.
	 */
	public static final KeyboardLayout CYRILLIC_UA = of(
			new String[] { "'", "1!", "2\"", "3№", "4;", "5%", "6:", "7?", "8*", "9(", "0)", "-_", "=+" },
			new String[] { "йЙ", "цЦ", "уУ", "кК", "еЕ", "нН", "гГ", "шШ", "щЩ", "зЗ", "хХ", "їЇ" },
			new String[] { "фФ", "іІ", "вВ", "аА", "пП", "рР", "оО", "лЛ", "дД", "жЖ", "єЄ", "ґҐ" },
			new String[] { "яЯ", "чЧ", "сС", "мМ", "иИ", "тТ", "ьЬ", "бБ", "юЮ", ".," }
	);

	private final Map<Character, int[]> keyPositions;

	private KeyboardLayout(Map<Character, int[]> keyPositions) {
		this.keyPositions = keyPositions;
	}

	/**
	 * Creates a custom keyboard layout from four rows of key groups.
	 * <p>
	 * Each row is an array of strings. Each string groups all characters produced by
	 * the same physical key — for example, {@code "aAą"} means that {@code a}, {@code A},
	 * and {@code ą} all occupy the same physical position.
	 * <p>
	 * Rows are ordered top to bottom (number row, top letter row, home row, bottom row).
	 * The standard keyboard row stagger is applied automatically.
	 *
	 * @param row0 number/symbol row (topmost)
	 * @param row1 top letter row
	 * @param row2 middle letter row
	 * @param row3 bottom letter row
	 * @return a new keyboard layout
	 */
	public static KeyboardLayout of(String[] row0, String[] row1, String[] row2, String[] row3) {
		String[][] rows = { row0, row1, row2, row3 };
		Map<Character, int[]> map = new HashMap<>();
		for ( int row = 0; row < rows.length; row++ ) {
			for ( int pos = 0; pos < rows[row].length; pos++ ) {
				int col = pos * KEY_WIDTH + ROW_OFFSETS[row];
				int[] coord = { row, col };
				for ( char c : rows[row][pos].toCharArray() ) {
					map.put( c, coord );
				}
			}
		}
		return new KeyboardLayout( Collections.unmodifiableMap( map ) );
	}

	/**
	 * Returns whether two characters occupy physically adjacent keys on this layout.
	 * Returns {@code false} if either character is not present in this layout.
	 *
	 * @param a the first character
	 * @param b the second character
	 * @return {@code true} if both characters are present and their keys are adjacent
	 */
	public boolean areAdjacent(char a, char b) {
		int[] posA = keyPositions.get( a );
		int[] posB = keyPositions.get( b );
		if ( posA == null || posB == null ) {
			return false;
		}
		int rowDiff = Math.abs( posA[0] - posB[0] );
		int colDiff = Math.abs( posA[1] - posB[1] );
		return rowDiff <= 1 && colDiff <= MAX_COL_DIFF && ( rowDiff != 0 || colDiff != 0 );
	}
}
