/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;

/**
 * Returns the declared methods of the specified class.
 *
 * @author Emmanuel Bernard
 */
public final class GetDeclaredMethods {

	private GetDeclaredMethods() {
	}

	public static Method[] action(Class<?> clazz) {
		return clazz.getDeclaredMethods();
	}

}
