/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraints.Null;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Manager in charge of providing and caching initialized {@code ConstraintValidator} instances.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorManager {
	private static final Log log = LoggerFactory.make();

	/**
	 * Dummy {@code ConstraintValidator} used as placeholder for the case that for a given context there exists
	 * no matching constraint validator instance
	 */
	private static ConstraintValidator<?, ?> DUMMY_CONSTRAINT_VALIDATOR = new ConstraintValidator<Null, Object>() {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	};

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
	 * Cache of initialized {@code ConstraintValidator} instances keyed against validates type, annotation and
	 * constraint validator factory ({@code CacheKey}).
	 */
	private final ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>> constraintValidatorCache;

	/**
	 * Creates a new {@code ConstraintValidatorManager}.
	 *
	 * @param constraintValidatorFactory the validator factory
	 */
	public ConstraintValidatorManager(ConstraintValidatorFactory constraintValidatorFactory) {
		this.defaultConstraintValidatorFactory = constraintValidatorFactory;
		this.constraintValidatorCache = new ConcurrentHashMap<>();
	}

	/**
	 * @param validatedValueType the type of the value to be validated. Cannot be {@code null}.
	 * @param descriptor the constraint descriptor for which to get an initalized constraint validator. Cannot be {@code null}
	 * @param constraintFactory constraint factory used to instantiate the constraint validator. Cannot be {@code null}.
	 * @param <A> the annotation type
	 *
	 * @return an initialized constraint validator for the given type and annotation of the value to be validated.
	 * {@code null} is returned if no matching constraint validator could be found.
	 */
	public <A extends Annotation> ConstraintValidator<A, ?> getInitializedValidator(Type validatedValueType,
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

		if ( constraintValidatorCache.containsKey( key ) ) {
			@SuppressWarnings("unchecked")
			ConstraintValidator<A, ?> constraintValidator = (ConstraintValidator<A, ?>) constraintValidatorCache.get(
					key
			);
			if ( DUMMY_CONSTRAINT_VALIDATOR.equals( constraintValidator ) ) {
				return null;
			}
			else {
				log.tracef( "Constraint validator %s found in cache.", constraintValidator );
				return constraintValidator;
			}
		}

		ConstraintValidatorDescriptor<A> validatorDescriptor = findMatchingValidatorDescriptor(
				descriptor,
				validatedValueType
		);
		ConstraintValidator<A, ?> constraintValidator = createAndInitializeValidator(
				constraintFactory,
				validatorDescriptor,
				descriptor
		);
		if ( constraintValidator == null ) {
			putInitializedValidator(
					validatedValueType,
					descriptor.getAnnotation(),
					constraintFactory,
					DUMMY_CONSTRAINT_VALIDATOR
			);
			return null;
		}
		else {
			putInitializedValidator(
					validatedValueType,
					descriptor.getAnnotation(),
					constraintFactory,
					constraintValidator
			);
			return constraintValidator;

		}
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

	private <A extends Annotation> ConstraintValidator<A, ?> createAndInitializeValidator(
			ConstraintValidatorFactory constraintFactory,
			ConstraintValidatorDescriptor<A> validatorDescriptor,
			ConstraintDescriptor<A> descriptor) {

		if ( validatorDescriptor == null ) {
			return null;
		}

		ConstraintValidator<A, ?> constraintValidator = validatorDescriptor.newInstance( constraintFactory );

		initializeConstraint( descriptor, constraintValidator );
		return constraintValidator;
	}

	private void clearEntriesForFactory(ConstraintValidatorFactory constraintFactory) {
		List<CacheKey> entriesToRemove = new ArrayList<>();
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
	private <A extends Annotation> ConstraintValidatorDescriptor<A> findMatchingValidatorDescriptor(ConstraintDescriptorImpl<A> descriptor, Type validatedValueType) {
		Map<Type, ConstraintValidatorDescriptor<A>> availableValidatorDescriptors = TypeHelper.getValidatorTypes(
				descriptor.getAnnotationType(),
				descriptor.getMatchingConstraintValidatorClasses()
		);

		List<Type> discoveredSuitableTypes = findSuitableValidatorTypes( validatedValueType, availableValidatorDescriptors.keySet() );
		resolveAssignableTypes( discoveredSuitableTypes );

		if ( discoveredSuitableTypes.size() == 0 ) {
			return null;
		}

		if ( discoveredSuitableTypes.size() > 1 ) {
			throw log.getMoreThanOneValidatorFoundForTypeException( validatedValueType, discoveredSuitableTypes );
		}

		Type suitableType = discoveredSuitableTypes.get( 0 );
		return availableValidatorDescriptors.get( suitableType );
	}

	private <A extends Annotation> List<Type> findSuitableValidatorTypes(Type type, Iterable<Type> availableValidatorTypes) {
		List<Type> determinedSuitableTypes = newArrayList();
		for ( Type validatorType : availableValidatorTypes ) {
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
		catch (RuntimeException e) {
			throw log.getUnableToInitializeConstraintValidatorException( constraintValidator.getClass(), e );
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

		List<Type> typesToRemove = new ArrayList<>();
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
