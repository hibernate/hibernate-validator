package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Field;

/**
 * Returns the fields of the specified class.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredFields {

	private GetDeclaredFields() {
	}

	public static Field[] action(Class<?> clazz) {
		return clazz.getDeclaredFields();
	}

}
