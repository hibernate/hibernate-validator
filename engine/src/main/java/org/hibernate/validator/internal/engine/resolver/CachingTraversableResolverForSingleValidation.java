/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;

/**
 * Cache results of a delegated traversable resolver to optimize calls.
 * It works only for a single validate* call and should not be used if
 * {@code TraversableResolver} is accessed concurrently.
 *
 * @author Emmanuel Bernard
 */
class CachingTraversableResolverForSingleValidation implements TraversableResolver {

	private final TraversableResolver delegate;

	private final Map<TraversableHolder, TraversableHolder> traversables = new HashMap<TraversableHolder, TraversableHolder>();

	public CachingTraversableResolverForSingleValidation(TraversableResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		TraversableHolder currentLH = new TraversableHolder( traversableObject, traversableProperty );
		TraversableHolder cachedLH = traversables.get( currentLH );
		if ( cachedLH == null ) {
			currentLH.isReachable = delegate.isReachable(
					traversableObject,
					traversableProperty,
					rootBeanType,
					pathToTraversableObject,
					elementType
			);
			traversables.put( currentLH, currentLH );
			cachedLH = currentLH;
		}
		else if ( cachedLH.isReachable == null ) {
			cachedLH.isReachable = delegate.isReachable(
					traversableObject,
					traversableProperty,
					rootBeanType,
					pathToTraversableObject,
					elementType
			);
		}
		return cachedLH.isReachable;
	}

	@Override
	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		TraversableHolder currentLH = new TraversableHolder( traversableObject, traversableProperty );

		TraversableHolder cachedLH = traversables.get( currentLH );
		if ( cachedLH == null ) {
			currentLH.isCascadable = delegate.isCascadable(
					traversableObject,
					traversableProperty,
					rootBeanType,
					pathToTraversableObject,
					elementType
			);
			traversables.put( currentLH, currentLH );
			cachedLH = currentLH;
		}
		else if ( cachedLH.isCascadable == null ) {
			cachedLH.isCascadable = delegate.isCascadable(
					traversableObject,
					traversableProperty,
					rootBeanType,
					pathToTraversableObject,
					elementType
			);
		}
		return cachedLH.isCascadable;
	}

	private static final class TraversableHolder extends AbstractTraversableHolder {
		private Boolean isReachable;
		private Boolean isCascadable;

		private TraversableHolder(Object traversableObject, Path.Node traversableProperty) {
			super( traversableObject, traversableProperty );
		}
	}
}
