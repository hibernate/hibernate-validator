/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.actions;

import java.net.URL;

/**
 * Loads the given resource.
 *
 * @author Gunnar Morling
 */
public final class GetResource {

	private GetResource() {
	}

	public static URL action(ClassLoader classLoader, String resourceName) {
		return classLoader.getResource( resourceName );
	}
}
