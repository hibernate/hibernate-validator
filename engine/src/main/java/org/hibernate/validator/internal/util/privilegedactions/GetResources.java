/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.privilegedactions;

import java.io.IOException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;

/**
 * A {@code PrivilegedAction} wrapping around {@code ClassLoader.getResources(String)}.
 *
 * @author Hardy Ferentschik
 */
public final class GetResources implements PrivilegedAction<Enumeration<URL>> {

	private final String resourceName;
	private final ClassLoader classLoader;

	public static GetResources action(ClassLoader classLoader, String resourceName) {
		return new GetResources( classLoader, resourceName );
	}

	private GetResources(ClassLoader classLoader, String resourceName) {
		this.classLoader = classLoader;
		this.resourceName = resourceName;
	}

	@Override
	public Enumeration<URL> run() {
		try {
			return classLoader.getResources( resourceName );
		}
		catch ( IOException e ) {
			// Collections.emptyEnumeration() would be 1.7
			return Collections.enumeration( Collections.<URL>emptyList() );
		}
	}
}
