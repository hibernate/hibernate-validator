/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

/**
 * Checks if an external class is present in the provided class loader.
 */
public final class IsClassPresent {

	private IsClassPresent() {
	}

	public static boolean action(String className, ClassLoader classLoader) {
		try {
			Class.forName( className, false, classLoader );
			return Boolean.TRUE;
		}
		catch (ClassNotFoundException e) {
			return Boolean.FALSE;
		}
	}
}
