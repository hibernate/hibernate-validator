/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.HashMap;

import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;

/**
 * Cache results of a delegated {@link JPATraversableResolver} to optimize calls.
 * <p>
 * It should only be used to wrap a {@code JPATraversableResolver} as it relies on the contract defined in the Bean
 * Validation specification.
 * <p>
 * It works only for a single validate* call and should not be used if {@code TraversableResolver} is accessed
 * concurrently.
 *
 * @author Guillaume Smet
 */
class CachingJPATraversableResolverForSingleValidation implements TraversableResolver {

	private final TraversableResolver delegate;

	private final HashMap<TraversableHolder, Boolean> traversables = new HashMap<TraversableHolder, Boolean>();

	public CachingJPATraversableResolverForSingleValidation(TraversableResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
			ElementType elementType) {
		if ( traversableObject == null ) {
			return true;
		}

		return traversables.computeIfAbsent( new TraversableHolder( traversableObject, traversableProperty ), th -> delegate.isReachable(
				traversableObject,
				traversableProperty,
				rootBeanType,
				pathToTraversableObject,
				elementType ) );
	}

	@Override
	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
			ElementType elementType) {
		// JPATraversableResolver returns true for isCascadable() per spec so we can avoid the overhead of caching.

		return true;
	}

	private static class TraversableHolder extends AbstractTraversableHolder {

		private TraversableHolder(Object traversableObject, Path.Node traversableProperty) {
			super( traversableObject, traversableProperty );
		}
	}
}
