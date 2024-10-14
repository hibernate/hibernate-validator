/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

/**
 * Checks if an external class is present in the provided class loader.
 */
public final class IsClassPresent {

	private IsClassPresent() {
	}

	public static boolean action(String className, ClassLoader classLoader) {
		try {
			Class.forName( className, false, classLoader );
			return Boolean.TRUE;
		}
		catch (ClassNotFoundException e) {
			return Boolean.FALSE;
		}
	}
}
