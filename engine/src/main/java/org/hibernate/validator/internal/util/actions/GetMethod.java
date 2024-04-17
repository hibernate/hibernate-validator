/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;

/**
 * Returns the method with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetMethod {

	private GetMethod() {
	}

	public static Method action(Class<?> clazz, String methodName) {
		try {
			return clazz.getMethod( methodName );
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}

}
