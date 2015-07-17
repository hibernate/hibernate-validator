/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.util.Arrays;

/**
 * Helper class dealing with strings.
 *
 * @author Gunnar Morling
 */
public class StringHelper {

	private StringHelper() {
	}

	/**
	 * Joins the elements of the given array to a string, separated by the given separator string.
	 *
	 * @param array the array to join
	 * @param separator the separator string
	 *
	 * @return a string made up of the string representations of the given array's members, separated by the given separator
	 *         string
	 */
	public static String join(Object[] array, String separator) {
		return array != null ? join( Arrays.asList( array ), separator ) : null;
	}

	/**
	 * Joins the elements of the given iterable to a string, separated by the given separator string.
	 *
	 * @param iterable the iterable to join
	 * @param separator the separator string
	 *
	 * @return a string made up of the string representations of the given iterable members, separated by the given separator
	 *         string
	 */
	public static String join(Iterable<?> iterable, String separator) {
		if ( iterable == null ) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;

		for ( Object object : iterable ) {
			if ( !isFirst ) {
				sb.append( separator );
			}
			else {
				isFirst = false;
			}

			sb.append( object );
		}

		return sb.toString();
	}

	/**
	 * Returns the given string, with its first letter changed to lower-case unless the string starts with more than
	 * one upper-case letter, in which case the string will be returned unaltered.
	 * <p>
	 * Provided to avoid a dependency on the {@link java.beans.Introspector} API which is not available on the Android
	 * platform (HV-779).
	 *
	 * @param string the string to decapitalize
	 *
	 * @return the given string, decapitalized. {@code null} is returned if {@code null} is passed as input; An empty
	 *         string is returned if an empty string is passed as input
	 *
	 * @see java.beans.Introspector#decapitalize(String)
	 */
	public static String decapitalize(String string) {
		if ( string == null || string.isEmpty() || startsWithSeveralUpperCaseLetters( string ) ) {
			return string;
		}
		else {
			return string.substring( 0, 1 ).toLowerCase() + string.substring( 1 );
		}
	}

	private static boolean startsWithSeveralUpperCaseLetters(String string) {
		return string.length() > 1 &&
				Character.isUpperCase( string.charAt( 0 ) ) &&
				Character.isUpperCase( string.charAt( 1 ) );
	}
}
