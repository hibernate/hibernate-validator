/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;
import java.lang.reflect.Method;

/**
 * Returns the declared methods of the specified class.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredMethods implements PrivilegedAction<Method[]> {
	private final Class<?> clazz;

	public static GetDeclaredMethods action(Class<?> clazz) {
		return new GetDeclaredMethods( clazz );
	}

	private GetDeclaredMethods(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Method[] run() {
		return clazz.getDeclaredMethods();
	}
}
