/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
