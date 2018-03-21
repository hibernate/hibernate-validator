/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraints.Null;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Manager in charge of providing and caching initialized {@code ConstraintValidator} instances.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Dummy {@code ConstraintValidator} used as placeholder for the case that for a given context there exists
	 * no matching constraint validator instance
	 */
	static ConstraintValidator<?, ?> DUMMY_CONSTRAINT_VALIDATOR = new ConstraintValidator<Null, Object>() {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	};

	/**
	 * The explicit or implicit default constraint validator factory. We always cache {@code ConstraintValidator}
	 * instances if they are created via the default instance and with the default initialization context. Constraint
	 * validator instances created via other factory instances (specified eg via {@code ValidatorFactory#usingContext()}
	 * or initialization context are only cached for the most recently used factory and context.
	 */
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;

	/**
	 * The explicit or implicit default constraint validator initialization context. We always cache
	 * {@code ConstraintValidator} instances if they are created via the default instance and with the default context.
	 * Constraint validator instances created via other factory instances (specified eg via
	 * {@code ValidatorFactory#usingContext()} or initialization context are only cached for the most recently used
	 * factory and context.
	 */
	private final  HibernateConstraintValidatorInitializationContext defaultConstraintValidatorInitializationContext;

	/**
	 * The most recently used non default constraint validator factory.
	 */
	private volatile ConstraintValidatorFactory mostRecentlyUsedNonDefaultConstraintValidatorFactory;

	/**
	 * The most recently used non default constraint validator initialization context.
	 */
	private volatile HibernateConstraintValidatorInitializationContext mostRecentlyUsedNonDefaultConstraintValidatorInitializationContext;

	/**
	 * Used for synchronizing access to {@link #mostRecentlyUsedNonDefaultConstraintValidatorFactory} (which can be
	 * null itself).
	 */
	private final Object mostRecentlyUsedNonDefaultConstraintValidatorFactoryAndInitializationContextMutex = new Object();

	/**
	 * Cache of initialized {@code ConstraintValidator} instances keyed against validated type, annotation,
	 * constraint validator factory and constraint validator initialization context ({@code CacheKey}).
	 */
	private final ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>> constraintValidatorCache;

	/**
	 * Creates a new {@code ConstraintValidatorManager}.
	 *
	 * @param defaultConstraintValidatorFactory the default validator factory
	 * @param defaultConstraintValidatorInitializationContext the default initialization context
	 */
	public ConstraintValidatorManager(ConstraintValidatorFactory defaultConstraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext defaultConstraintValidatorInitializationContext) {
		this.defaultConstraintValidatorFactory = defaultConstraintValidatorFactory;
		this.defaultConstraintValidatorInitializationContext = defaultConstraintValidatorInitializationContext;
		this.constraintValidatorCache = new ConcurrentHashMap<>();
	}

	/**
	 * @param validatedValueType the type of the value to be validated. Cannot be {@code null}.
	 * @param descriptor the constraint descriptor for which to get an initialized constraint validator. Cannot be {@code null}
	 * @param constraintValidatorFactory constraint factory used to instantiate the constraint validator. Cannot be {@code null}.
	 * @param initializationContext context used on constraint validator initialization
	 * @param <A> the annotation type
	 *
	 * @return an initialized constraint validator for the given type and annotation of the value to be validated.
	 * {@code null} is returned if no matching constraint validator could be found.
	 */
	public <A extends Annotation> ConstraintValidator<A, ?> getInitializedValidator(
			Type validatedValueType,
			ConstraintDescriptorImpl<A> descriptor,
			ConstraintValidatorFactory constraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		Contracts.assertNotNull( validatedValueType );
		Contracts.assertNotNull( descriptor );
		Contracts.assertNotNull( constraintValidatorFactory );
		Contracts.assertNotNull( initializationContext );

		CacheKey key = new CacheKey( descriptor.getAnnotationDescriptor(), validatedValueType, constraintValidatorFactory, initializationContext );

		@SuppressWarnings("unchecked")
		ConstraintValidator<A, ?> constraintValidator = (ConstraintValidator<A, ?>) constraintValidatorCache.get( key );

		if ( constraintValidator == null ) {
			constraintValidator = createAndInitializeValidator( validatedValueType, descriptor, constraintValidatorFactory, initializationContext );
			constraintValidator = cacheValidator( key, constraintValidator );
		}
		else {
			LOG.tracef( "Constraint validator %s found in cache.", constraintValidator );
		}

		return DUMMY_CONSTRAINT_VALIDATOR == constraintValidator ? null : constraintValidator;
	}

	private <A extends Annotation> ConstraintValidator<A, ?> cacheValidator(CacheKey key,
			ConstraintValidator<A, ?> constraintValidator) {
		// we only cache constraint validator instances for the default and most recently used factory
		if ( ( key.getConstraintValidatorFactory() != defaultConstraintValidatorFactory
				&& key.getConstraintValidatorFactory() != mostRecentlyUsedNonDefaultConstraintValidatorFactory ) ||
				( key.getConstraintValidatorInitializationContext() != defaultConstraintValidatorInitializationContext
						&& key.getConstraintValidatorInitializationContext() != mostRecentlyUsedNonDefaultConstraintValidatorInitializationContext ) ) {

			synchronized ( mostRecentlyUsedNonDefaultConstraintValidatorFactoryAndInitializationContextMutex ) {
				if ( key.constraintValidatorFactory != mostRecentlyUsedNonDefaultConstraintValidatorFactory ||
						key.constraintValidatorInitializationContext != mostRecentlyUsedNonDefaultConstraintValidatorInitializationContext ) {
					clearEntries( mostRecentlyUsedNonDefaultConstraintValidatorFactory, mostRecentlyUsedNonDefaultConstraintValidatorInitializationContext );
					mostRecentlyUsedNonDefaultConstraintValidatorFactory = key.getConstraintValidatorFactory();
					mostRecentlyUsedNonDefaultConstraintValidatorInitializationContext = key.getConstraintValidatorInitializationContext();
				}
			}
		}

		@SuppressWarnings("unchecked")
		ConstraintValidator<A, ?> cached = (ConstraintValidator<A, ?>) constraintValidatorCache.putIfAbsent( key, constraintValidator );

		return cached != null ? cached : constraintValidator;
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> ConstraintValidator<A, ?> createAndInitializeValidator(
			Type validatedValueType,
			ConstraintDescriptorImpl<A> descriptor,
			ConstraintValidatorFactory constraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext initializationContext) {

		ConstraintValidatorDescriptor<A> validatorDescriptor = findMatchingValidatorDescriptor( descriptor, validatedValueType );
		ConstraintValidator<A, ?> constraintValidator;

		if ( validatorDescriptor == null ) {
			constraintValidator = (ConstraintValidator<A, ?>) DUMMY_CONSTRAINT_VALIDATOR;
		}
		else {
			constraintValidator = validatorDescriptor.newInstance( constraintValidatorFactory );
			initializeValidator( descriptor, constraintValidator, initializationContext );
		}

		return constraintValidator;
	}

	private void clearEntries(ConstraintValidatorFactory constraintValidatorFactory, HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {
		Iterator<Entry<CacheKey, ConstraintValidator<?, ?>>> cacheEntries = constraintValidatorCache.entrySet().iterator();

		while ( cacheEntries.hasNext() ) {
			Entry<CacheKey, ConstraintValidator<?, ?>> cacheEntry = cacheEntries.next();
			if ( cacheEntry.getKey().getConstraintValidatorFactory() == constraintValidatorFactory &&
					cacheEntry.getKey().getConstraintValidatorInitializationContext() == constraintValidatorInitializationContext ) {
				constraintValidatorFactory.releaseInstance( cacheEntry.getValue() );
				cacheEntries.remove();
			}
		}
	}

	public void clear() {
		for ( Map.Entry<CacheKey, ConstraintValidator<?, ?>> entry : constraintValidatorCache.entrySet() ) {
			entry.getKey().getConstraintValidatorFactory().releaseInstance( entry.getValue() );
		}
		constraintValidatorCache.clear();
	}

	public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	public HibernateConstraintValidatorInitializationContext getDefaultConstraintValidatorInitializationContext() {
		return defaultConstraintValidatorInitializationContext;
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
				descriptor.getMatchingConstraintValidatorDescriptors()
		);

		List<Type> discoveredSuitableTypes = findSuitableValidatorTypes( validatedValueType, availableValidatorDescriptors.keySet() );
		resolveAssignableTypes( discoveredSuitableTypes );

		if ( discoveredSuitableTypes.size() == 0 ) {
			return null;
		}

		if ( discoveredSuitableTypes.size() > 1 ) {
			throw LOG.getMoreThanOneValidatorFoundForTypeException( validatedValueType, discoveredSuitableTypes );
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

	private <A extends Annotation> void initializeValidator(
			ConstraintDescriptor<A> descriptor,
			ConstraintValidator<A, ?> constraintValidator,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			if ( constraintValidator instanceof HibernateConstraintValidator ) {
				( (HibernateConstraintValidator<A, ?>) constraintValidator ).initialize( descriptor, initializationContext );
			}
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch (RuntimeException e) {
			if ( e instanceof ConstraintDeclarationException ) {
				throw e;
			}
			throw LOG.getUnableToInitializeConstraintValidatorException( constraintValidator.getClass(), e );
		}
	}

	/**
	 * Tries to reduce all assignable classes down to a single class.
	 *
	 * @param assignableTypes The set of all classes which are assignable to the class of the value to be validated and
	 * which are handled by at least one of the validators for the specified constraint.
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
		// These members are not final for optimization purposes
		private ConstraintAnnotationDescriptor<?> annotationDescriptor;
		private Type validatedType;
		private ConstraintValidatorFactory constraintValidatorFactory;
		private HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;
		private int hashCode;

		private CacheKey(ConstraintAnnotationDescriptor<?> annotationDescriptor, Type validatorType, ConstraintValidatorFactory constraintValidatorFactory,
				HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {
			this.annotationDescriptor = annotationDescriptor;
			this.validatedType = validatorType;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;
			this.hashCode = createHashCode();
		}

		public ConstraintValidatorFactory getConstraintValidatorFactory() {
			return constraintValidatorFactory;
		}

		public HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext() {
			return constraintValidatorInitializationContext;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			// no need to check for the type here considering it's only used in a typed map
			if ( o == null ) {
				return false;
			}

			CacheKey other = (CacheKey) o;

			if ( !annotationDescriptor.equals( other.annotationDescriptor ) ) {
				return false;
			}
			if ( !validatedType.equals( other.validatedType ) ) {
				return false;
			}
			if ( !constraintValidatorFactory.equals( other.constraintValidatorFactory ) ) {
				return false;
			}
			if ( !constraintValidatorInitializationContext.equals( other.constraintValidatorInitializationContext ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int createHashCode() {
			int result = annotationDescriptor.hashCode();
			result = 31 * result + validatedType.hashCode();
			result = 31 * result + constraintValidatorFactory.hashCode();
			result = 31 * result + constraintValidatorInitializationContext.hashCode();
			return result;
		}
	}
}
