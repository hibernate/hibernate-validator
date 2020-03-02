/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.constraints.Null;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Default implementation of the {@link ConstraintValidatorManager}.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ConstraintValidatorManagerImpl extends AbstractConstraintValidatorManagerImpl {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Dummy {@code ConstraintValidator} used as placeholder for the case that for a given context there exists
	 * no matching constraint validator instance
	 */
	private static final ConstraintValidator<?, ?> DUMMY_CONSTRAINT_VALIDATOR = new ConstraintValidator<Null, Object>() {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	};

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
	public ConstraintValidatorManagerImpl(ConstraintValidatorFactory defaultConstraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext defaultConstraintValidatorInitializationContext) {
		super( defaultConstraintValidatorFactory, defaultConstraintValidatorInitializationContext );
		this.constraintValidatorCache = new ConcurrentHashMap<>();
	}

	@Override
	public boolean isPredefinedScope() {
		return false;
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
	@Override
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
		if ( ( key.getConstraintValidatorFactory() != getDefaultConstraintValidatorFactory()
				&& key.getConstraintValidatorFactory() != mostRecentlyUsedNonDefaultConstraintValidatorFactory ) ||
				( key.getConstraintValidatorInitializationContext() != getDefaultConstraintValidatorInitializationContext()
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
		ConstraintValidator<A, ?> cached = (ConstraintValidator<A, ?>) constraintValidatorCache.putIfAbsent( key,
				constraintValidator != null ? constraintValidator : DUMMY_CONSTRAINT_VALIDATOR );

		return cached != null ? cached : constraintValidator;
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

	@Override
	public void clear() {
		for ( Map.Entry<CacheKey, ConstraintValidator<?, ?>> entry : constraintValidatorCache.entrySet() ) {
			entry.getKey().getConstraintValidatorFactory().releaseInstance( entry.getValue() );
		}
		constraintValidatorCache.clear();
	}

	public int numberOfCachedConstraintValidatorInstances() {
		return constraintValidatorCache.size();
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
