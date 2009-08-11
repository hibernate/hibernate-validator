package org.hibernate.validation.util;

import java.security.PrivilegedAction;
import java.lang.reflect.Method;

/**
 * @author Emmanuel Bernard
 */
public class GetDeclaredMethods implements PrivilegedAction<Method[]> {
	private final Class<?> clazz;

	public static GetDeclaredMethods action(Class<?> clazz) {
		return new GetDeclaredMethods( clazz );
	}

	private GetDeclaredMethods(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Method[] run() {
		return clazz.getDeclaredMethods();
	}
}