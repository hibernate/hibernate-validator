package org.hibernate.validation.util;

import java.security.PrivilegedAction;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * @author Emmanuel Bernard
 */
public class ContainsMethod implements PrivilegedAction<Boolean> {
	private final Class<?> clazz;
	private final String property;

	public static ContainsMethod action(Class<?> clazz, String property) {
		return new ContainsMethod( clazz, property );
	}

	private ContainsMethod(Class<?> clazz, String property) {
		this.clazz = clazz;
		this.property = property;
	}

	public Boolean run() {
		return ReflectionHelper.getMethod( clazz, property ) != null;
	}
}