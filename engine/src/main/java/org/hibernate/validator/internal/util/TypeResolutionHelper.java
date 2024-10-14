/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

import com.fasterxml.classmate.TypeResolver;

/**
 * Provides access to a shared instance of ClassMate's {@link TypeResolver}.
 *
 * @author Gunnar Morling
 */
public class TypeResolutionHelper {

	private final TypeResolver typeResolver;

	public TypeResolutionHelper() {
		typeResolver = new TypeResolver();
	}

	/**
	 * @return the typeResolver
	 */
	public TypeResolver getTypeResolver() {
		return typeResolver;
	}
}
