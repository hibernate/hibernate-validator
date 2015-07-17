/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Field;
import java.security.PrivilegedAction;

/**
 * Returns the declared field with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredField implements PrivilegedAction<Field> {
	private final Class<?> clazz;
	private final String fieldName;

	public static GetDeclaredField action(Class<?> clazz, String fieldName) {
		return new GetDeclaredField( clazz, fieldName );
	}

	private GetDeclaredField(Class<?> clazz, String fieldName) {
		this.clazz = clazz;
		this.fieldName = fieldName;
	}

	@Override
	public Field run() {
		try {
			final Field field = clazz.getDeclaredField( fieldName );
			field.setAccessible( true );
			return field;
		}
		catch ( NoSuchFieldException e ) {
			return null;
		}
	}
}
