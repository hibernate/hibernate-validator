/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.util;

import java.util.Arrays;

/**
 * Utilities used for message interpolation.
 * <p>
 * Except where documented otherwise, these utilities do not check their arguments for {@code null};
 * callers are responsible for passing non-{@code null} values.
 *
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class InterpolationHelper {

	public static final char BEGIN_TERM = '{';
	public static final char END_TERM = '}';
	public static final char EL_DESIGNATOR = '$';
	public static final char ESCAPE_CHARACTER = '\\';

	private InterpolationHelper() {
	}

	/**
	 * Escapes all characters that have a special meaning in message interpolation
	 * ({@code \}, <code>{</code>, <code>}</code>, {@code $}) by prefixing them with the escape
	 * character, so that they are treated as literals when the message is interpolated.
	 * <p>
	 * If the given value contains no escapable characters, it is returned as-is (same instance).
	 *
	 * @param messageParameter the value to escape; may be {@code null}
	 *
	 * @return the escaped value, or {@code null} if {@code messageParameter} was {@code null}
	 */
	public static String escapeMessageParameter(String messageParameter) {
		// deliberately not using patterns to try and save a bit on perf side
		if ( messageParameter == null ) {
			return null;
		}

		int firstSpecial = -1;
		for ( int i = 0; i < messageParameter.length(); i++ ) {
			if ( isEscapeableChar( messageParameter.charAt( i ) ) ) {
				firstSpecial = i;
				break;
			}
		}

		if ( firstSpecial < 0 ) {
			return messageParameter;
		}

		StringBuilder sb = new StringBuilder( messageParameter.length() + 8 );
		sb.append( messageParameter, 0, firstSpecial );

		for ( int i = firstSpecial; i < messageParameter.length(); i++ ) {
			char c = messageParameter.charAt( i );
			if ( isEscapeableChar( c ) ) {
				sb.append( ESCAPE_CHARACTER );
			}
			sb.append( c );
		}

		return sb.toString();
	}

	/**
	 * Reverses {@link #escapeMessageParameter(String)} by removing the escape character that precedes a
	 * character with a special meaning in message interpolation ({@code \}, <code>{</code>, <code>}</code>,
	 * {@code $}).
	 * <p>
	 * A backslash followed by any other character is left as-is, and a trailing lone backslash is preserved.
	 * Note that this method is not idempotent (e.g. <code>\\{</code> unescapes to <code>\{</code> in one pass
	 * but to <code>{</code> in two); it must be applied exactly once, at the end of message interpolation.
	 * <p>
	 * This method does not check its argument for {@code null}.
	 *
	 * @param message the escaped message; must not be {@code null}
	 *
	 * @return the unescaped message
	 *
	 * @throws NullPointerException if {@code message} is {@code null}
	 */
	public static String unescapeMessageParameter(String message) {
		int backslashIndex = message.indexOf( ESCAPE_CHARACTER );
		if ( backslashIndex < 0 ) {
			return message;
		}

		StringBuilder sb = new StringBuilder( message.length() );
		sb.append( message, 0, backslashIndex );

		// CHECKSTYLE:OFF: ModifiedControlVariable
		for ( int i = backslashIndex; i < message.length(); i++ ) {
			char c = message.charAt( i );
			if ( c == ESCAPE_CHARACTER && i + 1 < message.length() ) {
				char next = message.charAt( i + 1 );
				if ( isEscapeableChar( next ) ) {
					sb.append( next );
					i++;
					continue;
				}
			}
			sb.append( c );
		}
		// CHECKSTYLE:ON: ModifiedControlVariable

		return sb.toString();
	}

	/**
	 * Determines whether the given character has a special meaning in message interpolation and therefore
	 * needs to be escaped before being embedded into a message template.
	 *
	 * @param c the character to check
	 *
	 * @return {@code true} if {@code c} is one of {@code \}, <code>{</code>, <code>}</code> or {@code $}
	 */
	public static boolean isEscapeableChar(char c) {
		return c == ESCAPE_CHARACTER || c == BEGIN_TERM || c == END_TERM || c == EL_DESIGNATOR;
	}

	/**
	 * Converts the given value to its string representation. Array values are rendered using the
	 * corresponding {@link Arrays} {@code toString} overload (e.g. {@code [1, 2, 3]}); any other value is
	 * converted via {@link Object#toString()}.
	 * <p>
	 * This method does not check its argument for {@code null}.
	 *
	 * @param value the value to convert; must not be {@code null}
	 *
	 * @return the string representation of {@code value}
	 *
	 * @throws NullPointerException if {@code value} is {@code null}
	 */
	public static String resolveToString(Object value) {
		if ( value.getClass().isArray() ) {
			if ( value instanceof boolean[] booleans ) {
				return Arrays.toString( booleans );
			}
			else if ( value instanceof char[] chars ) {
				return Arrays.toString( chars );
			}
			else if ( value instanceof byte[] bytes ) {
				return Arrays.toString( bytes );
			}
			else if ( value instanceof short[] shorts ) {
				return Arrays.toString( shorts );
			}
			else if ( value instanceof int[] ints ) {
				return Arrays.toString( ints );
			}
			else if ( value instanceof long[] longs ) {
				return Arrays.toString( longs );
			}
			else if ( value instanceof float[] floats ) {
				return Arrays.toString( floats );
			}
			else if ( value instanceof double[] doubles ) {
				return Arrays.toString( doubles );
			}
			else {
				return Arrays.deepToString( (Object[]) value );
			}
		}
		return value.toString();
	}
}
