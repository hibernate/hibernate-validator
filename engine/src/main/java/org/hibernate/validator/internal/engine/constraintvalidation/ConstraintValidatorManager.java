/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

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
	 *
	 * @param constraintValidatorFactory the validator factory
	 */
	public ConstraintValidatorManager(ConstraintValidatorFactory constraintValidatorFactory) {
		this.defaultConstraintValidatorFactory = constraintValidatorFactory;
		this.constraintValidatorCache = new ConcurrentHashMap<CacheKey, ConstraintValidator<?, ?>>();
	}

	/**
	 * @param valueContext the current value context. Cannot be {@code null} including the validated type.
	 * @param descriptor the constraint descriptor for which to get an initalized constraint validator. Cannot be {@code null}
	 * @param constraintValidatorFactory constraint factory used to instantiate the constraint validator. Cannot be {@code null}.
	 * Could be {@code null}.
	 * @param <V> the type of the value to be validated
	 * @param <A> the annotation type
	 *
	 * @return an initialized constraint validator for the given type and annotation of the value to be validated.
	 */
	public <V, A extends Annotation> ConstraintValidator<A, V> getInitializedValidator(ValueContext<?, V> valueContext,
																					   ConstraintDescriptorImpl<A> descriptor,
																					   ConstraintValidatorFactory constraintValidatorFactory) {
		Contracts.assertNotNull( valueContext );
		Type typeOfValidatedElement = valueContext.getDeclaredTypeOfValidatedElement();
		Contracts.assertNotNull( typeOfValidatedElement );
		Contracts.assertNotNull( descriptor );
		Contracts.assertNotNull( constraintValidatorFactory );

		UnwrapMode valueUnwrapMode = valueContext.getUnwrapMode();
		final CacheKey key = new CacheKey(
				descriptor.getAnnotation(),
				typeOfValidatedElement,
				constraintValidatorFactory,
				valueUnwrapMode
		);

		@SuppressWarnings("unchecked")
		ConstraintValidator<A, V> constraintValidator = (ConstraintValidator<A, V>) constraintValidatorCache.get( key );

		if ( constraintValidator == null ) {
			Class<? extends ConstraintValidator<?, ?>> validatorClass = findMatchingValidatorClass(
					valueContext,
					descriptor
			);
			constraintValidator = createAndInitializeValidator(
					constraintValidatorFactory,
					validatorClass,
					descriptor
			);
			putInitializedValidator(
					typeOfValidatedElement,
					descriptor.getAnnotation(),
					constraintValidatorFactory,
					constraintValidator,
					valueUnwrapMode

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
										 ConstraintValidator<?, ?> constraintValidator,
										 UnwrapMode unwrapMode) {
		// we only cache constraint validator instance for the default and least recently used factory
		if ( constraintFactory != defaultConstraintValidatorFactory && constraintFactory != leastRecentlyUsedNonDefaultConstraintValidatorFactory ) {
			clearEntriesForFactory( leastRecentlyUsedNonDefaultConstraintValidatorFactory );
			leastRecentlyUsedNonDefaultConstraintValidatorFactory = constraintFactory;
		}

		final CacheKey key = new CacheKey(
				annotation,
				validatedValueType,
				constraintFactory,
				unwrapMode
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
	 * @param valueContext The current value context
	 * @param descriptor The constraint descriptor for the constraint to be applied
	 *
	 * @return The class of a matching validator.
	 */
	private <A extends Annotation> Class<? extends ConstraintValidator<A, ?>>
	findMatchingValidatorClass(ValueContext<?, ?> valueContext, ConstraintDescriptorImpl<A> descriptor) {
		Map<Type, Class<? extends ConstraintValidator<A, ?>>> availableValidatorTypes = descriptor.getAvailableValidatorTypes();
		Type typeOfValidatedElement = valueContext.getDeclaredTypeOfValidatedElement();

		// find constraint validator classes which can directly validate the value to validate
		List<Type> suitableTypesForValidatedValue = findSuitableValidatorTypes(
				typeOfValidatedElement,
				availableValidatorTypes
		);
		resolveAssignableTypes( suitableTypesForValidatedValue );

		// if we also have a suitable value unwrapper we resolve for the type provided by the unwrapper as well
		List<Type> suitableTypesForWrappedValue;
		ValidatedValueUnwrapper<?> validatedValueHandler = valueContext.getValidatedValueHandler();
		if ( validatedValueHandler != null) {
			Type unwrappedType = validatedValueHandler.getValidatedValueType( typeOfValidatedElement );
			suitableTypesForWrappedValue = findSuitableValidatorTypes( unwrappedType, availableValidatorTypes );
			resolveAssignableTypes( suitableTypesForWrappedValue );
		}
		else {
			suitableTypesForWrappedValue = Collections.emptyList();
		}

		Type suitableType = verifyResolveWasUnique(
				valueContext,
				descriptor,
				suitableTypesForValidatedValue,
				suitableTypesForWrappedValue
		);

		return availableValidatorTypes.get( suitableType );
	}

	private Type verifyResolveWasUnique(ValueContext<?, ?> valueContext,
										ConstraintDescriptorImpl<?> descriptor,
										List<Type> constraintValidatorTypesForValidatedValue,
										List<Type> constraintValidatorTypesForWrappedValue) {
		TypeResolutionResult typeResolutionResult = typeResolutionOutcome(
				constraintValidatorTypesForValidatedValue,
				constraintValidatorTypesForWrappedValue,
				valueContext
		);
		switch ( typeResolutionResult ) {
			case VALIDATORS_FOR_VALIDATED_VALUE_AND_WRAPPED_VALUE: {
				throw log.getConstraintValidatorExistsForWrapperAndWrappedValueException(
						valueContext.getPropertyPath().toString(),
						descriptor.getAnnotationType().getName(),
						valueContext.getValidatedValueHandler().getClass().getName()
				);
			}
			case SINGLE_VALIDATOR_FOR_VALIDATED_VALUE: {
				return constraintValidatorTypesForValidatedValue.get( 0 );
			}
			case MULTIPLE_VALIDATORS_FOR_VALIDATED_VALUE: {
				throw log.getMoreThanOneValidatorFoundForTypeException(
						getValidatedValueTypeForErrorReporting( valueContext ),
						StringHelper.join( constraintValidatorTypesForValidatedValue, "," )
				);
			}
			case SINGLE_VALIDATOR_FOR_WRAPPED_VALUE: {
				return constraintValidatorTypesForWrappedValue.get( 0 );
			}
			case NO_VALIDATORS: {
				if ( descriptor.getConstraintType() == ConstraintType.CROSS_PARAMETER ) {
					throw log.getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(
							descriptor.getAnnotationType().getName()
					);
				}
				else {
					Type typeOfValidatedElement = getValidatedValueTypeForErrorReporting( valueContext );
					String validatedValueClassName = typeOfValidatedElement.toString();
					if ( typeOfValidatedElement instanceof Class ) {
						Class<?> clazz = (Class<?>) typeOfValidatedElement;
						if ( clazz.isArray() ) {
							validatedValueClassName = clazz.getComponentType().toString() + "[]";
						}
						else {
							validatedValueClassName = clazz.getName();
						}
					}
					throw log.getNoValidatorFoundForTypeException(
							descriptor.getAnnotationType().getName(),
							validatedValueClassName,
							valueContext.getPropertyPath().toString()
					);
				}
			}
			case MULTIPLE_VALIDATORS_FOR_WRAPPED_VALUE: {
				// can currently not happen, because if there are multiple suitable unwrappers the first matching one
				// is chosen. See ValidationContext#getValidatedValueHandler (HF)
				break;
			}
		}
		return null;
	}

	private Type getValidatedValueTypeForErrorReporting(ValueContext<?, ?> valueContext) {
		Type typeOfValidatedElement = valueContext.getDeclaredTypeOfValidatedElement();

		// for the sake of better reporting we unwrap if there is a unwrapper in the context
		if ( valueContext.getValidatedValueHandler() != null ) {
			typeOfValidatedElement = valueContext.getCurrentValidatedValue().getClass();
		}
		return typeOfValidatedElement;
	}

	private TypeResolutionResult typeResolutionOutcome(List<Type> constraintValidatorTypesForValidatedValue,
													   List<Type> constraintValidatorTypesForWrappedValue,
													   ValueContext<?, ?> valueContext) {
		if ( UnwrapMode.UNWRAP.equals( valueContext.getUnwrapMode() )
				&& constraintValidatorTypesForWrappedValue.size() == 0 ) {
			throw log.getNoUnwrapperFoundForTypeException( valueContext.getDeclaredTypeOfValidatedElement().toString() );
		}

		if ( constraintValidatorTypesForValidatedValue.size() > 0 && constraintValidatorTypesForWrappedValue.size() > 0 ) {
			switch ( valueContext.getUnwrapMode() ) {
				case AUTOMATIC: {
					return TypeResolutionResult.VALIDATORS_FOR_VALIDATED_VALUE_AND_WRAPPED_VALUE;
				}
				case UNWRAP: {
					return getTypeResolutionResultForWrappedValue( constraintValidatorTypesForWrappedValue );
				}
				case SKIP_UNWRAP: {
					// explicitly set the value handler to null. Is there a better place to do this? (HF)
					valueContext.setValidatedValueHandler( null );
					return getTypeResolutionResultForValidatedValue( valueContext, constraintValidatorTypesForValidatedValue );
				}
			}

		}

		if ( constraintValidatorTypesForWrappedValue.size() == 0 ) {
			return getTypeResolutionResultForValidatedValue( valueContext, constraintValidatorTypesForValidatedValue );
		}

		if ( constraintValidatorTypesForValidatedValue.size() == 0 ) {
			return getTypeResolutionResultForWrappedValue( constraintValidatorTypesForWrappedValue );
		}

		return null;
	}

	private ConstraintValidatorManager.TypeResolutionResult getTypeResolutionResultForWrappedValue(List<Type> constraintValidatorTypesForWrappedValue) {
		if ( constraintValidatorTypesForWrappedValue.size() == 1 ) {
			return ConstraintValidatorManager.TypeResolutionResult.SINGLE_VALIDATOR_FOR_WRAPPED_VALUE;
		}
		else {
			return ConstraintValidatorManager.TypeResolutionResult.MULTIPLE_VALIDATORS_FOR_WRAPPED_VALUE;
		}
	}

	private ConstraintValidatorManager.TypeResolutionResult getTypeResolutionResultForValidatedValue(ValueContext<?, ?> valueContext,
			List<Type> constraintValidatorTypesForValidatedValue) {
		if ( constraintValidatorTypesForValidatedValue.size() == 0 ) {
			return ConstraintValidatorManager.TypeResolutionResult.NO_VALIDATORS;
		}
		else if ( constraintValidatorTypesForValidatedValue.size() == 1 ) {
			// explicitly set the value handler to null. Is there a better place to do this? (HF)
			valueContext.setValidatedValueHandler( null );
			return ConstraintValidatorManager.TypeResolutionResult.SINGLE_VALIDATOR_FOR_VALIDATED_VALUE;
		}
		else {
			return ConstraintValidatorManager.TypeResolutionResult.MULTIPLE_VALIDATORS_FOR_VALIDATED_VALUE;
		}
	}

	private <A extends Annotation> List<Type> findSuitableValidatorTypes(Type type,
			Map<Type, Class<? extends ConstraintValidator<A, ?>>> availableValidatorTypes) {
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
		private final UnwrapMode unwrapMode;
		private final int hashCode;

		private CacheKey(Annotation annotation,
						 Type validatorType,
						 ConstraintValidatorFactory constraintFactory,
						 UnwrapMode unwrapMode) {
			this.annotation = annotation;
			this.validatedType = validatorType;
			this.constraintFactory = constraintFactory;
			this.unwrapMode = unwrapMode;
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

			if ( hashCode != cacheKey.hashCode ) {
				return false;
			}
			if ( !annotation.equals( cacheKey.annotation ) ) {
				return false;
			}
			if ( !constraintFactory.equals( cacheKey.constraintFactory ) ) {
				return false;
			}
			if ( unwrapMode != cacheKey.unwrapMode ) {
				return false;
			}
			if ( !validatedType.equals( cacheKey.validatedType ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int createHashCode() {
			int result = annotation.hashCode();
			result = 31 * result + validatedType.hashCode();
			result = 31 * result + constraintFactory.hashCode();
			result = 31 * result + unwrapMode.hashCode();
			result = 31 * result + hashCode;
			return result;
		}
	}

	private static enum TypeResolutionResult {
		SINGLE_VALIDATOR_FOR_VALIDATED_VALUE,
		MULTIPLE_VALIDATORS_FOR_VALIDATED_VALUE,
		SINGLE_VALIDATOR_FOR_WRAPPED_VALUE,
		MULTIPLE_VALIDATORS_FOR_WRAPPED_VALUE,
		VALIDATORS_FOR_VALIDATED_VALUE_AND_WRAPPED_VALUE,
		NO_VALIDATORS
	}
}
