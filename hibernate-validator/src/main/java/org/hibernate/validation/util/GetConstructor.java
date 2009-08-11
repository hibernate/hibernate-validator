package org.hibernate.validation.util;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 */
public class GetConstructor<T> implements PrivilegedAction<Constructor<T>> {
	private final Class<T> clazz;
	private final Class<?>[] params;

	public static <T> GetConstructor<T> action(Class<T> clazz, Class<?>... params) {
		return new GetConstructor<T>( clazz, params );
	}

	private GetConstructor(Class<T> clazz, Class<?>... params) {
		this.clazz = clazz;
		this.params = params;
	}

	public Constructor<T> run() {
		try {
			return clazz.getConstructor(params);
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}