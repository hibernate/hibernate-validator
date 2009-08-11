package org.hibernate.validation.util;

import java.security.PrivilegedAction;
import java.lang.reflect.Field;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * @author Emmanuel Bernard
 */
public class GetDeclaredField implements PrivilegedAction<Field> {
	private final Class<?> clazz;
	private final String fieldName;

	public static GetDeclaredField action(Class<?> clazz, String fieldName) {
		return new GetDeclaredField( clazz, fieldName );
	}

	private GetDeclaredField(Class<?> clazz, String fieldName) {
		this.clazz = clazz;
		this.fieldName = fieldName;
	}

	public Field run() {
		try {
			final Field field = clazz.getDeclaredField( fieldName );
			ReflectionHelper.setAccessibility( field );
			return field;
		}
		catch ( NoSuchFieldException e ) {
			return null;
		}
	}
}