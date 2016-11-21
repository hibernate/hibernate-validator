/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.Contracts;

/**
 * Privileged action used to set the Thread context class loader.
 *
 * @author Guillaume Smet
 */
public final class SetClassLoader implements PrivilegedAction<Void> {
	private final ClassLoader classLoader;

	public static SetClassLoader ofContext(ClassLoader classLoader) {
		Contracts.assertNotNull( classLoader, "class loader must not be null" );
		return new SetClassLoader( classLoader );
	}

	private SetClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public Void run() {
		Thread.currentThread().setContextClassLoader( classLoader );
		return null;
	}
}
