/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;
import java.lang.reflect.Field;

/**
 * Returns the fields of the specified class.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredFields implements PrivilegedAction<Field[]> {
	private final Class<?> clazz;

	public static GetDeclaredFields action(Class<?> clazz) {
		return new GetDeclaredFields( clazz );
	}

	private GetDeclaredFields(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Field[] run() {
		return clazz.getDeclaredFields();
	}
}
