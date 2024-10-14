/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import org.hibernate.validator.internal.util.Contracts;

/**
 * Privileged action used to set the Thread context class loader.
 *
 * @author Guillaume Smet
 */
public final class SetContextClassLoader {

	private SetContextClassLoader() {
	}

	public static void action(ClassLoader classLoader) {
		Contracts.assertNotNull( classLoader, "class loader must not be null" );
		Thread.currentThread().setContextClassLoader( classLoader );
	}
}
