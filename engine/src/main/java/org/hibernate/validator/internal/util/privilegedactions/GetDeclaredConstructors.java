/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

/**
 * Returns the declared constructors of the specified class.
 *
 * @author Gunnar Morling
 */
public final class GetDeclaredConstructors implements PrivilegedAction<Constructor<?>[]> {
	private final Class<?> clazz;

	public static GetDeclaredConstructors action(Class<?> clazz) {
		return new GetDeclaredConstructors( clazz );
	}

	private GetDeclaredConstructors(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Constructor<?>[] run() {
		return clazz.getDeclaredConstructors();
	}
}
