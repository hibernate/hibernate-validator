/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.lang.annotation.ElementType;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;

/**
 * A dummy traversable resolver which returns always {@code true}. This resolver is used by default by all test cases.
 *
 * @author Hardy Ferentschik
 */
public class DummyTraversableResolver implements TraversableResolver {
	@Override
	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return true;
	}

	@Override
	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return true;
	}
}


