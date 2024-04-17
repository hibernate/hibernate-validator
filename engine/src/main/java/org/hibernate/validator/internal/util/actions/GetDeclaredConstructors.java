/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Constructor;

/**
 * Returns the declared constructors of the specified class.
 *
 * @author Gunnar Morling
 */
public final class GetDeclaredConstructors {

	private GetDeclaredConstructors() {
	}

	public static Constructor<?>[] action(Class<?> clazz) {
		return clazz.getDeclaredConstructors();
	}
}
