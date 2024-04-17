/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
