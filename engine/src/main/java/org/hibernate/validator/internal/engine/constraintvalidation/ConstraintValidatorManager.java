/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

/**
 * Manager in charge of providing and caching initialized {@code ConstraintValidator} instances.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorManager {
	private final ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>> constraintValidatorCache;

	/**
	 * Creates a new {@code ConstraintValidatorManager}.
	 */
	public ConstraintValidatorManager() {
		this.constraintValidatorCache = new ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>>();
	}

	/**
	 * @param validatedValueType the type of the value to be validated
	 * @param annotation the constraint annotation
	 * @param constraintFactory constraint factory used to instantiate the constraint validator
	 *
	 * @return A initialized constraint validator for the given type and annotation of the value to be validated.
	 */
	public ConstraintValidator<?, ?> getInitializedValidator(Type validatedValueType,
															 Annotation annotation,
															 ConstraintValidatorFactory constraintFactory) {

		final CacheKey key = new CacheKey(
				annotation,
				validatedValueType,
				constraintFactory
		);

		return constraintValidatorCache.get( key );
	}

	public void putInitializedValidator(Type validatedValueType,
										Annotation annotation,
										ConstraintValidatorFactory constraintFactory,
										ConstraintValidator<?, ?> constraintValidator) {

		final CacheKey key = new CacheKey(
				annotation,
				validatedValueType,
				constraintFactory
		);

		constraintValidatorCache.putIfAbsent( key, constraintValidator );
	}

	public void clear() {
		for ( Map.Entry<CacheKey, ConstraintValidator<?, ?>> entry : constraintValidatorCache.entrySet() ) {
			entry.getKey().getConstraintFactory().releaseInstance( entry.getValue() );
		}
		constraintValidatorCache.clear();
	}

	public int numberOfCachedConstraintValidatorInstances() {
		return constraintValidatorCache.size();
	}

	private static final class CacheKey {
		private final Annotation annotation;
		private final Type validatedType;
		private final ConstraintValidatorFactory constraintFactory;

		private CacheKey(Annotation annotation, Type validatorType, ConstraintValidatorFactory constraintFactory) {
			this.annotation = annotation;
			this.validatedType = validatorType;
			this.constraintFactory = constraintFactory;
		}

		public ConstraintValidatorFactory getConstraintFactory() {
			return constraintFactory;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			CacheKey cacheKey = (CacheKey) o;

			if ( annotation != null ? !annotation.equals( cacheKey.annotation ) : cacheKey.annotation != null ) {
				return false;
			}
			if ( constraintFactory != null ? !constraintFactory.equals( cacheKey.constraintFactory ) : cacheKey.constraintFactory != null ) {
				return false;
			}
			if ( validatedType != null ? !validatedType.equals( cacheKey.validatedType ) : cacheKey.validatedType != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = annotation != null ? annotation.hashCode() : 0;
			result = 31 * result + ( validatedType != null ? validatedType.hashCode() : 0 );
			result = 31 * result + ( constraintFactory != null ? constraintFactory.hashCode() : 0 );
			return result;
		}
	}
}
