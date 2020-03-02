/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;

import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.TraversableResolver;


/**
 * {@link TraversableResolver} considering that all properties are reachable and cascadable.
 * <p>
 * This is the default behavior if JPA is not detected in the classpath.
 *
 * @author Guillaume Smet
 */
class TraverseAllTraversableResolver implements TraversableResolver {

	TraverseAllTraversableResolver() {
	}

	@Override
	public boolean isReachable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
			ElementType elementType) {
		return true;
	}

	@Override
	public boolean isCascadable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
			ElementType elementType) {
		return true;
	}
}
