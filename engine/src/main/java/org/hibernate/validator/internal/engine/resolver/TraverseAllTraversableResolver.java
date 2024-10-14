/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;

import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.TraversableResolver;

/**
 * {@link TraversableResolver} considering that all properties are reachable and cascadable.
 * <p>
 * This is the default behavior if Jakarta Persistence is not detected in the classpath.
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
