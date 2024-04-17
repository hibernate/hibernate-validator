/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
