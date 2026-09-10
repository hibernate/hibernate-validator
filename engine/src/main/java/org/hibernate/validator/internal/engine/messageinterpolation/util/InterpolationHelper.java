/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern ESCAPE_MESSAGE_PARAMETER_PATTERN = Pattern.compile( "([\\" + ESCAPE_CHARACTER + BEGIN_TERM + END_TERM + EL_DESIGNATOR + "])" );

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
		if ( messageParameter == null ) {
			return null;
		}
		return ESCAPE_MESSAGE_PARAMETER_PATTERN.matcher( messageParameter ).replaceAll( Matcher.quoteReplacement( String.valueOf( ESCAPE_CHARACTER ) ) + "$1" );
	}

	private static final Pattern LEFT_BRACE = Pattern.compile( "\\{", Pattern.LITERAL );
	private static final Pattern RIGHT_BRACE = Pattern.compile( "\\}", Pattern.LITERAL );
	private static final Pattern SLASH = Pattern.compile( "\\\\", Pattern.LITERAL );
	private static final Pattern DOLLAR = Pattern.compile( "\\$", Pattern.LITERAL );

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
		if ( message.indexOf( '\\' ) > -1 ) {
			message = LEFT_BRACE.matcher( message ).replaceAll( "{" );
			message = RIGHT_BRACE.matcher( message ).replaceAll( "}" );
			message = SLASH.matcher( message ).replaceAll( Matcher.quoteReplacement( "\\" ) );
			message = DOLLAR.matcher( message ).replaceAll( Matcher.quoteReplacement( "$" ) );
		}
		return message;
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
		final String resolvedExpression;
		if ( value.getClass().isArray() ) {
			if ( value.getClass() == boolean[].class ) {
				resolvedExpression = Arrays.toString( (boolean[]) value );
			}
			else if ( value.getClass() == char[].class ) {
				resolvedExpression = Arrays.toString( (char[]) value );
			}
			else if ( value.getClass() == byte[].class ) {
				resolvedExpression = Arrays.toString( (byte[]) value );
			}
			else if ( value.getClass() == short[].class ) {
				resolvedExpression = Arrays.toString( (short[]) value );
			}
			else if ( value.getClass() == int[].class ) {
				resolvedExpression = Arrays.toString( (int[]) value );
			}
			else if ( value.getClass() == long[].class ) {
				resolvedExpression = Arrays.toString( (long[]) value );
			}
			else if ( value.getClass() == float[].class ) {
				resolvedExpression = Arrays.toString( (float[]) value );
			}
			else if ( value.getClass() == double[].class ) {
				resolvedExpression = Arrays.toString( (double[]) value );
			}
			else {
				resolvedExpression = Arrays.deepToString( (Object[]) value );
			}
		}
		else {
			resolvedExpression = value.toString();
		}
		return resolvedExpression;
	}
}
