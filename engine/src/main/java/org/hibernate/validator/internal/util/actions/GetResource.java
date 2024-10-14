/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
