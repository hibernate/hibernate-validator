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
	private final boolean makeAccessible;

	public static GetDeclaredField action(Class<?> clazz, String fieldName) {
		return new GetDeclaredField( clazz, fieldName, false );
	}

	/**
	 * Before using this method, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager.
	 */
	public static GetDeclaredField andMakeAccessible(Class<?> clazz, String fieldName) {
		return new GetDeclaredField( clazz, fieldName, true );
	}

	private GetDeclaredField(Class<?> clazz, String fieldName, boolean makeAccessible) {
		this.clazz = clazz;
		this.fieldName = fieldName;
		this.makeAccessible = makeAccessible;
	}

	@Override
	public Field run() {
		try {
			final Field field = clazz.getDeclaredField( fieldName );
			if ( makeAccessible ) {
				field.setAccessible( true );
			}
			return field;
		}
		catch (NoSuchFieldException e) {
			return null;
		}
	}
}
