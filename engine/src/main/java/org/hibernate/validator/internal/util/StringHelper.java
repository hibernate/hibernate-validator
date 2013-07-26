/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
}
