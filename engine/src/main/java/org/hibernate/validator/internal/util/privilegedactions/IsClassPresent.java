/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

/**
 * Checks if an external class is present in the provided class loader.
 */
public final class IsClassPresent implements PrivilegedAction<Boolean> {

	private final String className;

	private final ClassLoader classLoader;

	public static IsClassPresent action(String className, ClassLoader classLoader) {
		return new IsClassPresent( className, classLoader );
	}

	private IsClassPresent(String className, ClassLoader classLoader) {
		this.className = className;
		this.classLoader = classLoader;
	}

	@Override
	public Boolean run() {
		try {
			Class.forName( className, false, classLoader );
			return Boolean.TRUE;
		}
		catch (ClassNotFoundException e) {
			return Boolean.FALSE;
		}
	}
}
