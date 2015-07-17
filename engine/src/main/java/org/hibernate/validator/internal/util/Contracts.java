/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
public final class Contracts {

	private static final Log log = LoggerFactory.make();

	private Contracts() {
	}

	public static void assertNotNull(Object o) {
		assertNotNull( o, MESSAGES.mustNotBeNull() );
	}

	/**
	 * Asserts that the given object is not {@code null}.
	 *
	 * @param o The object to check.
	 * @param message A message text which will be used as message of the resulting
	 * exception if the given object is {@code null}.
	 *
	 * @throws IllegalArgumentException In case the given object is {@code null}.
	 */
	public static void assertNotNull(Object o, String message) {
		if ( o == null ) {
			throw log.getIllegalArgumentException( message );
		}
	}

	/**
	 * Asserts that the given object is not {@code null}.
	 *
	 * @param o The object to check.
	 * @param name The name of the value to check. A message of the form
	 * "&lt;name&gt; must not be null" will be used as message of
	 * the resulting exception if the given object is {@code null}.
	 *
	 * @throws IllegalArgumentException In case the given object is {@code null}.
	 */
	public static void assertValueNotNull(Object o, String name) {
		if ( o == null ) {
			throw log.getIllegalArgumentException( MESSAGES.mustNotBeNull( name ) );
		}
	}

	public static void assertTrue(boolean condition, String message) {
		if ( !condition ) {
			throw log.getIllegalArgumentException( message );
		}
	}

	public static void assertNotEmpty(String s, String message) {
		if ( s.length() == 0 ) {
			throw log.getIllegalArgumentException( message );
		}
	}
}
