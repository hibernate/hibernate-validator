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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Manager in charge of providing and caching initialized {@code ConstraintValidator} instances.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorManager {
	private static final Log log = LoggerFactory.make();

	/**
	 * The explicit or implicit default constraint validator factory. We always cache {@code ConstraintValidator} instances
	 * if they are created via the default instance. Constraint validator instances created via other factory
	 * instances (specified eg via {@code ValidatorFactory#usingContext()} are only cached for the least recently used
	 * factory
	 */
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;

	/**
	 * The least recently used non default constraint validator factory.
	 */
	private ConstraintValidatorFactory leastRecentlyUsedNonDefaultConstraintValidatorFactory;

	/**
	 * Cache of initalized {@code ConstraintValidator} instances keyed against validates type, annotation and
	 * constraint validator factory ({@code CacheKey}).
	 */
	private final ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>> constraintValidatorCache;

	/**
	 * Creates a new {@code ConstraintValidatorManager}.
	 */
	public ConstraintValidatorManager(ConstraintValidatorFactory constraintValidatorFactory) {
		this.defaultConstraintValidatorFactory = constraintValidatorFactory;
		this.constraintValidatorCache = new ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>>();
	}

	/**
	 * @param validatedValueType the type of the value to be validated. Cannot be {@code null}
	 * @param descriptor the constraint descriptor for which to get an initalized constraint validator. Cannot be {@code null}
	 * @param constraintFactory constraint factory used to instantiate the constraint validator. Cannot be {@code null}.
	 *
	 * @return A initialized constraint validator for the given type and annotation of the value to be validated.
	 */
	public <V, A extends Annotation> ConstraintValidator<A, V> getInitializedValidator(Type validatedValueType,
			ConstraintDescriptorImpl<A> descriptor,
			ConstraintValidatorFactory constraintFactory) {
		Contracts.assertNotNull( validatedValueType );
		Contracts.assertNotNull( descriptor );
		Contracts.assertNotNull( constraintFactory );

		final CacheKey key = new CacheKey(
				descriptor.getAnnotation(),
				validatedValueType,
				constraintFactory
		);

		@SuppressWarnings("unchecked")
		ConstraintValidator<A, V> constraintValidator = (ConstraintValidator<A, V>) constraintValidatorCache.get( key );

		if ( constraintValidator == null ) {
			Class<? extends ConstraintValidator<?, ?>> validatorClass = findMatchingValidatorClass(
					descriptor,
					validatedValueType
			);
			constraintValidator = createAndInitializeValidator( constraintFactory, validatorClass, descriptor );
			putInitializedValidator(
					validatedValueType,
					descriptor.getAnnotation(),
					constraintFactory,
					constraintValidator
			);
		}
		else {
			log.tracef( "Constraint validator %s found in cache.", constraintValidator );
		}

		return constraintValidator;
	}


	private void putInitializedValidator(Type validatedValueType,
			Annotation annotation,
			ConstraintValidatorFactory constraintFactory,
			ConstraintValidator<?, ?> constraintValidator) {
		// we only cache constraint validator instance for the default and least recently used factory
		if ( constraintFactory != defaultConstraintValidatorFactory && constraintFactory != leastRecentlyUsedNonDefaultConstraintValidatorFactory ) {
			clearEntriesForFactory( leastRecentlyUsedNonDefaultConstraintValidatorFactory );
			leastRecentlyUsedNonDefaultConstraintValidatorFactory = constraintFactory;
		}

		final CacheKey key = new CacheKey(
				annotation,
				validatedValueType,
				constraintFactory
		);

		constraintValidatorCache.putIfAbsent( key, constraintValidator );
	}

	private <V, A extends Annotation> ConstraintValidator<A, V> createAndInitializeValidator(
			ConstraintValidatorFactory constraintFactory,
			Class<? extends ConstraintValidator<?, ?>> validatorClass,
			ConstraintDescriptor<A> descriptor) {
		@SuppressWarnings("unchecked")
		ConstraintValidator<A, V> constraintValidator = (ConstraintValidator<A, V>) constraintFactory.getInstance(
				validatorClass
		);
		if ( constraintValidator == null ) {
			throw log.getConstraintFactoryMustNotReturnNullException( validatorClass.getName() );
		}
		initializeConstraint( descriptor, constraintValidator );
		return constraintValidator;
	}

	private void clearEntriesForFactory(ConstraintValidatorFactory constraintFactory) {
		List<CacheKey> entriesToRemove = new ArrayList<CacheKey>();
		for ( Map.Entry<CacheKey, ConstraintValidator<?, ?>> entry : constraintValidatorCache.entrySet() ) {
			if ( entry.getKey().getConstraintFactory() == constraintFactory ) {
				entriesToRemove.add( entry.getKey() );
			}
		}
		for ( CacheKey key : entriesToRemove ) {
			constraintValidatorCache.remove( key );
		}
	}

	public void clear() {
		for ( Map.Entry<CacheKey, ConstraintValidator<?, ?>> entry : constraintValidatorCache.entrySet() ) {
			entry.getKey().getConstraintFactory().releaseInstance( entry.getValue() );
		}
		constraintValidatorCache.clear();
	}

	public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	public int numberOfCachedConstraintValidatorInstances() {
		return constraintValidatorCache.size();
	}

	/**
	 * Runs the validator resolution algorithm.
	 *
	 * @param validatedValueType The type of the value to be validated (the type of the member/class the constraint was placed on).
	 *
	 * @return The class of a matching validator.
	 */
	private <A extends Annotation> Class<? extends ConstraintValidator<A, ?>> findMatchingValidatorClass(ConstraintDescriptorImpl<A> descriptor, Type validatedValueType) {
		Map<Type, Class<? extends ConstraintValidator<A, ?>>> availableValidatorTypes = TypeHelper.getValidatorsTypes(
				descriptor.getAnnotationType(),
				descriptor.getMatchingConstraintValidatorClasses()
		);

		List<Type> discoveredSuitableTypes = findSuitableValidatorTypes( validatedValueType, availableValidatorTypes );
		resolveAssignableTypes( discoveredSuitableTypes );
		verifyResolveWasUnique( descriptor, validatedValueType, discoveredSuitableTypes );

		Type suitableType = discoveredSuitableTypes.get( 0 );
		return availableValidatorTypes.get( suitableType );
	}

	private void verifyResolveWasUnique(ConstraintDescriptorImpl<?> descriptor, Type valueClass, List<Type> assignableClasses) {
		if ( assignableClasses.size() == 0 ) {
			if ( descriptor.getConstraintType() == ConstraintType.CROSS_PARAMETER ) {
				throw log.getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(
						descriptor.getAnnotationType()
								.getName()
				);
			}
			else {
				String className = valueClass.toString();
				if ( valueClass instanceof Class ) {
					Class<?> clazz = (Class<?>) valueClass;
					if ( clazz.isArray() ) {
						className = clazz.getComponentType().toString() + "[]";
					}
					else {
						className = clazz.getName();
					}
				}
				throw log.getNoValidatorFoundForTypeException( className );
			}
		}
		else if ( assignableClasses.size() > 1 ) {
			StringBuilder builder = new StringBuilder();
			for ( Type clazz : assignableClasses ) {
				builder.append( clazz );
				builder.append( ", " );
			}
			builder.delete( builder.length() - 2, builder.length() );
			throw log.getMoreThanOneValidatorFoundForTypeException( valueClass, builder.toString() );
		}
	}

	private <A extends Annotation> List<Type> findSuitableValidatorTypes(Type type, Map<Type, Class<? extends ConstraintValidator<A, ?>>> availableValidatorTypes) {
		List<Type> determinedSuitableTypes = newArrayList();
		for ( Type validatorType : availableValidatorTypes.keySet() ) {
			if ( TypeHelper.isAssignable( validatorType, type )
					&& !determinedSuitableTypes.contains( validatorType ) ) {
				determinedSuitableTypes.add( validatorType );
			}
		}
		return determinedSuitableTypes;
	}

	private <A extends Annotation> void initializeConstraint(ConstraintDescriptor<A> descriptor, ConstraintValidator<A, ?> constraintValidator) {
		try {
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToInitializeConstraintValidatorException( constraintValidator.getClass().getName(), e );
		}
	}

	/**
	 * Tries to reduce all assignable classes down to a single class.
	 *
	 * @param assignableTypes The set of all classes which are assignable to the class of the value to be validated and
	 * which are handled by at least one of the  validators for the specified constraint.
	 */
	private void resolveAssignableTypes(List<Type> assignableTypes) {
		if ( assignableTypes.size() == 0 || assignableTypes.size() == 1 ) {
			return;
		}

		List<Type> typesToRemove = new ArrayList<Type>();
		do {
			typesToRemove.clear();
			Type type = assignableTypes.get( 0 );
			for ( int i = 1; i < assignableTypes.size(); i++ ) {
				if ( TypeHelper.isAssignable( type, assignableTypes.get( i ) ) ) {
					typesToRemove.add( type );
				}
				else if ( TypeHelper.isAssignable( assignableTypes.get( i ), type ) ) {
					typesToRemove.add( assignableTypes.get( i ) );
				}
			}
			assignableTypes.removeAll( typesToRemove );
		} while ( typesToRemove.size() > 0 );
	}

	private static final class CacheKey {
		private final Annotation annotation;
		private final Type validatedType;
		private final ConstraintValidatorFactory constraintFactory;
		private final int hashCode;

		private CacheKey(Annotation annotation, Type validatorType, ConstraintValidatorFactory constraintFactory) {
			this.annotation = annotation;
			this.validatedType = validatorType;
			this.constraintFactory = constraintFactory;
			this.hashCode = createHashCode();
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
			return hashCode;
		}

		private int createHashCode() {
			int result = annotation != null ? annotation.hashCode() : 0;
			result = 31 * result + ( validatedType != null ? validatedType.hashCode() : 0 );
			result = 31 * result + ( constraintFactory != null ? constraintFactory.hashCode() : 0 );
			return result;
		}
	}
}
