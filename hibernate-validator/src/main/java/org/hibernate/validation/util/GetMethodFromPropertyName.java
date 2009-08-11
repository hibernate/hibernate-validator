package org.hibernate.validation.util;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * @author Emmanuel Bernard
 */
public class GetMethodFromPropertyName implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String property;

	public static GetMethodFromPropertyName action(Class<?> clazz, String property) {
		return new GetMethodFromPropertyName( clazz, property );
	}

	private GetMethodFromPropertyName(Class<?> clazz, String property) {
		this.clazz = clazz;
		this.property = property;
	}

	public Method run() {
			return ReflectionHelper.getMethod( clazz, property );
	}
}