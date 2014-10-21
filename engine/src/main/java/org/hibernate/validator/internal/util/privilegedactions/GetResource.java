/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.privilegedactions;

import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Loads the given resource.
 *
 * @author Gunnar Morling
 */
public final class GetResource implements PrivilegedAction<URL> {

	private final String resourceName;
	private final ClassLoader classLoader;

	public static GetResource action(ClassLoader classLoader, String resourceName) {
		return new GetResource( classLoader, resourceName );
	}

	private GetResource(ClassLoader classLoader, String resourceName) {
		this.classLoader = classLoader;
		this.resourceName = resourceName;
	}

	@Override
	public URL run() {
		return classLoader.getResource( resourceName );
	}
}
