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
 * Returns the methods of the specified class (include inherited methods).
 *
 * @author Emmanuel Bernard
 */
public final class GetMethods implements PrivilegedAction<Method[]> {
	private final Class<?> clazz;

	public static GetMethods action(Class<?> clazz) {
		return new GetMethods( clazz );
	}

	private GetMethods(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Method[] run() {
		return clazz.getMethods();
	}
}
