/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Constructor;

/**
 * Returns the declared constructor with the specified parameter types or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredConstructor {

	private GetDeclaredConstructor() {
	}

	public static <T> Constructor<T> action(Class<T> clazz, Class<?>... params) {
		try {
			return clazz.getDeclaredConstructor( params );
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}
}
