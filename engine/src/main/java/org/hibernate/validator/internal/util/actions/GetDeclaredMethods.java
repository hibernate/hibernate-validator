/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;

/**
 * Returns the declared methods of the specified class.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredMethods {

	private GetDeclaredMethods() {
	}

	public static Method[] action(Class<?> clazz) {
		return clazz.getDeclaredMethods();
	}

}
