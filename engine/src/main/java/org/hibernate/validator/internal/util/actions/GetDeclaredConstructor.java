/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Constructor;

/**
 * Returns the declared constructor with the specified parameter types or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredConstructor {

	private GetDeclaredConstructor() {
	}

	public static <T> Constructor<T> action(Class<T> clazz, Class<?>... params) {
		try {
			return clazz.getDeclaredConstructor( params );
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}
}
