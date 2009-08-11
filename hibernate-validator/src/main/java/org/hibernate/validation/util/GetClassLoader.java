package org.hibernate.validation.util;

import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 */
public class GetClassLoader implements PrivilegedAction<ClassLoader> {
	private final Class<?> clazz;

	public static GetClassLoader fromContext() {
		return new GetClassLoader( null );
	}

	public static GetClassLoader fromClass(Class<?> clazz) {
		if ( clazz == null ) throw new IllegalArgumentException("Class is null");
		return new GetClassLoader( clazz );
	}

	private GetClassLoader(Class<?> clazz) {
		this.clazz = clazz;
	}

	public ClassLoader run() {
		if (clazz != null) {
			return clazz.getClassLoader();
		}
		else {
			return Thread.currentThread().getContextClassLoader();
		}
	}
}