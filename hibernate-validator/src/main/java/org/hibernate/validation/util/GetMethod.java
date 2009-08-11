package org.hibernate.validation.util;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 */
public class GetMethod implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String methodName;

	public static GetMethod action(Class<?> clazz, String methodName) {
		return new GetMethod( clazz, methodName );
	}

	private GetMethod(Class<?> clazz, String methodName) {
		this.clazz = clazz;
		this.methodName = methodName;
	}

	public Method run() {
		try {
			return clazz.getMethod(methodName);
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
