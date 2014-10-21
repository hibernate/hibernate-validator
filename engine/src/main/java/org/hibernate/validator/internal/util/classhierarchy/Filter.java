/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.classhierarchy;

/**
 * A filter to be used when invoking
 * {@link ClassHierarchyHelper#getHierarchy(Class, Filter...)}.
 *
 * @author Gunnar Morling
 */
public interface Filter {

	/**
	 * Whether the given class is accepted by this filter or not.
	 *
	 * @param clazz the class of interest
	 *
	 * @return {@code true} if this filter accepts the given class (meaning it
	 *         will be contained in the result of
	 *         {@link ClassHierarchyHelper#getHierarchy(Class, Filter...)},
	 *         {@code false} otherwise.
	 */
	boolean accepts(Class<?> clazz);
}
