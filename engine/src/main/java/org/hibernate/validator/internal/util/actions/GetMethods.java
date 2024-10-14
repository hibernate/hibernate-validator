/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;

/**
 * Returns the methods of the specified class (include inherited methods).
 *
 * @author Emmanuel Bernard
 */
public final class GetMethods {

	private GetMethods() {
	}

	public static Method[] action(Class<?> clazz) {
		return clazz.getMethods();
	}
}
