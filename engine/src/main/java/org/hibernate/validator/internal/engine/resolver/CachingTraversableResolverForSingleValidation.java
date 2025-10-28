/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
		TraversableHolder currentLH = new TraversableHolder( traversableObject, traversableProperty, elementType );
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
		TraversableHolder currentLH = new TraversableHolder( traversableObject, traversableProperty, elementType );

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

	private static final class TraversableHolder {
		private final Object traversableObject;
		private final String traversableProperty;
		private final ElementType elementType;
		private final int hashCode;

		private Boolean isReachable;
		private Boolean isCascadable;

		private TraversableHolder(Object traversableObject, Path.Node traversableProperty, ElementType elementType) {
			this.traversableObject = traversableObject;
			// nodes and paths are mutable, do not use them for caching:
			this.traversableProperty = traversableProperty.getName();
			this.elementType = elementType;
			this.hashCode = buildHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || !( o instanceof TraversableHolder that ) ) {
				return false;
			}
			if ( traversableObject != null ? ( traversableObject != that.traversableObject ) : that.traversableObject != null ) {
				return false;
			}
			if ( !traversableProperty.equals( that.traversableProperty ) ) {
				return false;
			}
			return elementType == that.elementType;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int buildHashCode() {
			// HV-1013 Using identity hash code in order to avoid calling hashCode() of objects which may
			// be handling null properties not correctly
			int result = traversableObject != null ? System.identityHashCode( traversableObject ) : 0;
			result = 31 * result + traversableProperty.hashCode();
			result = 31 * result + elementType.hashCode();
			return result;
		}
	}
}
