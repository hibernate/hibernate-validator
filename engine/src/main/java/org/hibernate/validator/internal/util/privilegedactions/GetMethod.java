/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

/**
 * Returns the method with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetMethod implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String methodName;

	public static GetMethod action(Class<?> clazz, String methodName) {
		return new GetMethod( clazz, methodName );
	}

	private GetMethod(Class<?> clazz, String methodName) {
		this.clazz = clazz;
		this.methodName = methodName;
	}

	@Override
	public Method run() {
		try {
			return clazz.getMethod( methodName );
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
