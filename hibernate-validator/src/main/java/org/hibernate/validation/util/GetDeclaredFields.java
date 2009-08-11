package org.hibernate.validation.util;

import java.security.PrivilegedAction;
import java.lang.reflect.Field;

/**
 * @author Emmanuel Bernard
 */
public class GetDeclaredFields implements PrivilegedAction<Field[]> {
	private final Class<?> clazz;

	public static GetDeclaredFields action(Class<?> clazz) {
		return new GetDeclaredFields( clazz );
	}

	private GetDeclaredFields(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Field[] run() {
		return clazz.getDeclaredFields();
	}
}