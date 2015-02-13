/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

/**
 * @author Hardy Ferentschik
 */
public class MyCustomStringImpl implements CharSequence {
	private final String myString;

	public MyCustomStringImpl(String s) {
		this.myString = s;
	}

	public int length() {
		return myString.length();
	}

	public char charAt(int i) {
		return myString.charAt( i );
	}

	public CharSequence subSequence(int i, int j) {
		return myString.subSequence( i, j );
	}

	public String toString() {
		return myString;
	}
}
