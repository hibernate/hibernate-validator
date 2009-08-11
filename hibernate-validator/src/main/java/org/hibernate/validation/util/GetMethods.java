package org.hibernate.validation.util;

import java.security.PrivilegedAction;
import java.lang.reflect.Method;

/**
 * @author Emmanuel Bernard
 */
public class GetMethods implements PrivilegedAction<Method[]> {
	private final Class<?> clazz;

	public static GetMethods action(Class<?> clazz) {
		return new GetMethods( clazz );
	}

	private GetMethods(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Method[] run() {
		return clazz.getMethods();
	}
}
