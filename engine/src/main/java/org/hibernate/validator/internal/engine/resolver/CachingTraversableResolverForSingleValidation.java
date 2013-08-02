/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.engine.resolver;

import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Path;
import javax.validation.TraversableResolver;

/**
 * Cache results of a delegated traversable resolver to optimize calls.
 * It works only for a single validate* call and should not be used if
 * {@code TraversableResolver} is accessed concurrently.
 *
 * @author Emmanuel Bernard
 */
public class CachingTraversableResolverForSingleValidation implements TraversableResolver {
	private TraversableResolver delegate;
	private Map<TraversableHolder, TraversableHolder> traversables = new HashMap<TraversableHolder, TraversableHolder>();

	public CachingTraversableResolverForSingleValidation(TraversableResolver delegate) {
		this.delegate = delegate;
	}

	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		TraversableHolder currentLH = new TraversableHolder(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
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

	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		TraversableHolder currentLH = new TraversableHolder(
				traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType
		);
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
		private final Path.Node traversableProperty;
		private final Class<?> rootBeanType;
		private final Path pathToTraversableObject;
		private final ElementType elementType;
		private final int hashCode;

		private Boolean isReachable;
		private Boolean isCascadable;


		private TraversableHolder(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			this.traversableObject = traversableObject;
			this.traversableProperty = traversableProperty;
			this.rootBeanType = rootBeanType;
			this.pathToTraversableObject = pathToTraversableObject;
			this.elementType = elementType;
			this.hashCode = buildHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			TraversableHolder that = (TraversableHolder) o;

			if ( elementType != that.elementType ) {
				return false;
			}
			if ( !pathToTraversableObject.equals( that.pathToTraversableObject ) ) {
				return false;
			}
			if ( !rootBeanType.equals( that.rootBeanType ) ) {
				return false;
			}
			if ( traversableObject != null ? !traversableObject.equals( that.traversableObject ) : that.traversableObject != null ) {
				return false;
			}
			if ( !traversableProperty.equals( that.traversableProperty ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		public int buildHashCode() {
			int result = traversableObject != null ? traversableObject.hashCode() : 0;
			result = 31 * result + traversableProperty.hashCode();
			result = 31 * result + rootBeanType.hashCode();
			result = 31 * result + pathToTraversableObject.hashCode();
			result = 31 * result + elementType.hashCode();
			return result;
		}
	}
}
