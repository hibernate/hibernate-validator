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
 * Returns the declared constructor with the specified parameter types or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredConstructor<T> implements PrivilegedAction<Constructor<T>> {
	private final Class<T> clazz;
	private final Class<?>[] params;

	public static <T> GetDeclaredConstructor<T> action(Class<T> clazz, Class<?>... params) {
		return new GetDeclaredConstructor<T>( clazz, params );
	}

	private GetDeclaredConstructor(Class<T> clazz, Class<?>... params) {
		this.clazz = clazz;
		this.params = params;
	}

	@Override
	public Constructor<T> run() {
		try {
			return clazz.getDeclaredConstructor( params );
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
