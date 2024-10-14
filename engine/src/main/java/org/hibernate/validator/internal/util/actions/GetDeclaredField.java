/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Field;

/**
 * Returns the declared field with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredField {

	private GetDeclaredField() {
	}

	public static Field action(Class<?> clazz, String fieldName) {
		return action( clazz, fieldName, false );
	}

	/**
	 * Before using this method, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager.
	 */
	public static Field andMakeAccessible(Class<?> clazz, String fieldName) {
		return action( clazz, fieldName, true );
	}

	private static Field action(Class<?> clazz, String fieldName, boolean makeAccessible) {
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
