/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.actions;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * A {@code PrivilegedAction} wrapping around {@code ClassLoader.getResources(String)}.
 *
 * @author Hardy Ferentschik
 */
public final class GetResources {

	private GetResources() {
	}

	public static Enumeration<URL> action(ClassLoader classLoader, String resourceName) {
		try {
			return classLoader.getResources( resourceName );
		}
		catch (IOException e) {
			// Collections.emptyEnumeration() would be 1.7
			return Collections.enumeration( Collections.<URL>emptyList() );
		}
	}
}
