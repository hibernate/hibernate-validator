/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.internal.engine.path.MessageAndPath;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;
import static org.hibernate.validator.constraints.CompositionType.AND;
import static org.hibernate.validator.constraints.CompositionType.OR;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;

/**
 * Due to constraint composition a single constraint annotation can lead to a whole constraint tree being validated.
 * This class encapsulates such a tree.
 *
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2012 SERLI
 */
public class ConstraintTree<A extends Annotation> {
	private static final Log log = LoggerFactory.make();

	/**
	 * The default initial capacity for this cache.
	 */
	static final int DEFAULT_INITIAL_CAPACITY = 16;

	private final ConstraintTree<?> parent;
	private final List<ConstraintTree<?>> children;

	/**
	 * The constraint descriptor for the constraint represented by this constraint tree.
	 */
	private final ConstraintDescriptorImpl<A> descriptor;

	/**
	 * Cache for initialized {@code ConstraintValidator} instance per {@code ConstraintValidatorFactory} and validated type.
	 * This is necessary, because via {@code aValidatorFactory.usingContext().constraintValidatorFactory(otherCVF)} it
	 * is possible to create a {@code Validator} instance which is using a different constraint validator factory.
	 * <p>
	 * Also note, that {@code ConstraintTree} is part of the {@code BeanMetaData}
	 * which is cached on {@code ValidatorFactory} level. In most cases there is only one {@code ConstraintValidator}
	 * instance per {@code ConstraintTree}, unless {@code aValidatorFactory.usingContext().constraintValidatorFactory(otherCVF)} is
	 * used or the user classes are getting reloaded.
	 * </p>
	 */
	private final ConcurrentReferenceHashMap<ConstraintValidatorCacheKey, ConstraintValidator<A, ?>> constraintValidatorCache;

	public ConstraintTree(ConstraintDescriptorImpl<A> descriptor) {
		this( descriptor, null );
	}

	private ConstraintTree(ConstraintDescriptorImpl<A> descriptor, ConstraintTree<?> parent) {
		this.parent = parent;
		this.descriptor = descriptor;
		this.constraintValidatorCache =
				new ConcurrentReferenceHashMap<ConstraintValidatorCacheKey, ConstraintValidator<A, ?>>(
						DEFAULT_INITIAL_CAPACITY,
						SOFT,
						SOFT
				);

		final Set<ConstraintDescriptorImpl<?>> composingConstraints = newHashSet();
		for ( ConstraintDescriptor<?> composingConstraint : descriptor.getComposingConstraints() ) {
			composingConstraints.add( (ConstraintDescriptorImpl<?>) composingConstraint );
		}

		children = new ArrayList<ConstraintTree<?>>( composingConstraints.size() );

		for ( ConstraintDescriptorImpl<?> composingDescriptor : composingConstraints ) {
			ConstraintTree<?> treeNode = createConstraintTree( composingDescriptor );
			children.add( treeNode );
		}
	}

	private <U extends Annotation> ConstraintTree<U> createConstraintTree(ConstraintDescriptorImpl<U> composingDescriptor) {
		return new ConstraintTree<U>( composingDescriptor, this );
	}

	public final List<ConstraintTree<?>> getChildren() {
		return children;
	}

	public final ConstraintDescriptorImpl<A> getDescriptor() {
		return descriptor;
	}

	public final <T, U, V, E extends ConstraintViolation<T>> boolean validateConstraints(ValidationContext<T, E> executionContext, ValueContext<U, V> valueContext) {
		Set<E> constraintViolations = CollectionHelper.newHashSet();
		validateConstraints( executionContext, valueContext, constraintViolations );
		if ( !constraintViolations.isEmpty() ) {
			executionContext.addConstraintFailures( constraintViolations );
			return false;
		}
		return true;
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraints(ValidationContext<T, E> executionContext,
																				 ValueContext<U, V> valueContext,
																				 Set<E> constraintViolations) {
		CompositionResult compositionResult = validateComposingConstraints(
				executionContext, valueContext, constraintViolations
		);

		Set<E> localViolationList = CollectionHelper.newHashSet();

		// After all children are validated the actual ConstraintValidator of the constraint itself is executed (provided
		// there is one)
		// If fail fast mode is enabled and there are already failing constraints we don't need to validate the constraint (HV-550)
		if ( !descriptor.getConstraintValidatorClasses().isEmpty()
				&& ( !executionContext.isFailFastModeEnabled() || constraintViolations.isEmpty() ) ) {

			if ( log.isTraceEnabled() ) {
				log.tracef(
						"Validating value %s against constraint defined by %s.",
						valueContext.getCurrentValidatedValue(),
						descriptor
				);
			}
			ConstraintValidator<A, V> validator = getInitializedValidator(
					valueContext.getTypeOfAnnotatedElement(),
					executionContext.getConstraintValidatorFactory()
			);

			ConstraintValidatorContextImpl constraintValidatorContext = new ConstraintValidatorContextImpl(
					valueContext.getPropertyPath(), descriptor
			);

			validateSingleConstraint(
					executionContext,
					valueContext,
					constraintValidatorContext,
					validator,
					localViolationList
			);

			// We re-evaluate the boolean composition by taking into consideration also the violations
			// from the local constraintValidator
			if ( localViolationList.isEmpty() ) {
				compositionResult.setAtLeastOneTrue( true );
			}
			else {
				compositionResult.setAllTrue( false );
			}
		}

		if ( !passesCompositionTypeRequirement( constraintViolations, compositionResult ) ) {
			prepareFinalConstraintViolations(
					executionContext, valueContext, constraintViolations, localViolationList
			);
		}
	}

	/**
	 * Before the final constraint violations can be reported back we need to check whether we have a composing
	 * constraint whose result should be reported as single violation.
	 *
	 * @param executionContext Meta data about top level validation
	 * @param valueContext Meta data for currently validated value
	 * @param constraintViolations Used to accumulate constraint violations
	 * @param localViolationList List of constraint violations of top level constraint
	 */
	private <T, U, V, E extends ConstraintViolation<T>> void prepareFinalConstraintViolations(ValidationContext<T, E> executionContext, ValueContext<U, V> valueContext, Set<E> constraintViolations, Set<E> localViolationList) {
		if ( reportAsSingleViolation() ) {
			// We clear the current violations list anyway
			constraintViolations.clear();

			// But then we need to distinguish whether the local ConstraintValidator has reported
			// violations or not (or if there is no local ConstraintValidator at all).
			// If not we create a violation
			// using the error message in the annotation declaration at top level.
			if ( localViolationList.isEmpty() ) {
				final String message = (String) getDescriptor().getAttributes().get( "message" );
				MessageAndPath messageAndPath = new MessageAndPath( message, valueContext.getPropertyPath() );
				E violation = executionContext.createConstraintViolation(
						valueContext, messageAndPath, descriptor
				);
				constraintViolations.add( violation );
			}
		}

		// Now, if there were some violations reported by
		// the local ConstraintValidator, they need to be added to constraintViolations.
		// Whether we need to report them as a single constraint or just add them to the other violations
		// from the composing constraints, has been taken care of in the previous conditional block.
		// This takes also care of possible custom error messages created by the constraintValidator,
		// as checked in test CustomErrorMessage.java
		// If no violations have been reported from the local ConstraintValidator, or no such validator exists,
		// then we just add an empty list.
		constraintViolations.addAll( localViolationList );
	}

	/**
	 * Validates all composing constraints recursively.
	 *
	 * @param executionContext Meta data about top level validation
	 * @param valueContext Meta data for currently validated value
	 * @param constraintViolations Used to accumulate constraint violations
	 *
	 * @return Returns an instance of {@code CompositionResult} relevant for boolean composition of constraints
	 */
	private <T, U, V, E extends ConstraintViolation<T>> CompositionResult validateComposingConstraints(ValidationContext<T, E> executionContext,
																									   ValueContext<U, V> valueContext,
																									   Set<E> constraintViolations) {
		CompositionResult compositionResult = new CompositionResult( true, false );
		for ( ConstraintTree<?> tree : getChildren() ) {
			Set<E> tmpViolationList = CollectionHelper.newHashSet();
			tree.validateConstraints( executionContext, valueContext, tmpViolationList );
			constraintViolations.addAll( tmpViolationList );

			if ( tmpViolationList.isEmpty() ) {
				compositionResult.setAtLeastOneTrue( true );
				// no need to further validate constraints, because at least one validation passed
				if ( descriptor.getCompositionType() == OR ) {
					break;
				}
			}
			else {
				compositionResult.setAllTrue( false );
				if ( executionContext.isFailFastModeEnabled() && descriptor.getCompositionType() == AND ) {
					break;
				}
			}
		}
		return compositionResult;
	}

	private boolean passesCompositionTypeRequirement(Set<?> constraintViolations, CompositionResult compositionResult) {
		CompositionType compositionType = getDescriptor().getCompositionType();
		boolean passedValidation = false;
		switch ( compositionType ) {
			case OR:
				passedValidation = compositionResult.isAtLeastOneTrue();
				break;
			case AND:
				passedValidation = compositionResult.isAllTrue();
				break;
			case ALL_FALSE:
				passedValidation = !compositionResult.isAtLeastOneTrue();
				break;
		}
		assert ( !passedValidation || !( compositionType == AND ) || constraintViolations.isEmpty() );
		if ( passedValidation ) {
			constraintViolations.clear();
		}
		return passedValidation;
	}

	private <T, U, V, E extends ConstraintViolation<T>> Set<E> validateSingleConstraint(ValidationContext<T, E> executionContext,
																						ValueContext<U, V> valueContext,
																						ConstraintValidatorContextImpl constraintValidatorContext,
																						ConstraintValidator<A, V> validator,
																						Set<E> constraintViolations) {
		boolean isValid;
		try {
			isValid = validator.isValid( valueContext.getCurrentValidatedValue(), constraintValidatorContext );
		}
		catch ( RuntimeException e ) {
			throw log.getExceptionDuringIsValidCall( e );
		}
		if ( !isValid ) {
			//We do not add them these violations yet, since we don't know how they are
			//going to influence the final boolean evaluation
			constraintViolations.addAll(
					executionContext.createConstraintViolations(
							valueContext, constraintValidatorContext
					)
			);
		}
		return constraintViolations;
	}

	/**
	 * @return {@code} true if the current constraint should be reported as single violation, {@code false otherwise}.
	 *         When using negation, we only report the single top-level violation, as
	 *         it is hard, especially for ALL_FALSE to give meaningful reports
	 */
	private boolean reportAsSingleViolation() {
		return getDescriptor().isReportAsSingleViolation()
				|| getDescriptor().getCompositionType() == ALL_FALSE;
	}

	/**
	 * @param validatedValueType The type of the value to be validated (the type of the member/class the constraint was placed on).
	 * @param constraintFactory constraint factory used to instantiate the constraint validator.
	 *
	 * @return A initialized constraint validator matching the type of the value to be validated.
	 */
	@SuppressWarnings("unchecked")
	private <V> ConstraintValidator<A, V> getInitializedValidator(Type validatedValueType, ConstraintValidatorFactory constraintFactory) {

		final ConstraintValidatorCacheKey key = new ConstraintValidatorCacheKey(
				constraintFactory,
				validatedValueType
		);
		ConstraintValidator<A, V> constraintValidator = (ConstraintValidator<A, V>) constraintValidatorCache.get( key );
		if ( constraintValidator == null ) {
			Class<? extends ConstraintValidator<?, ?>> validatorClass = findMatchingValidatorClass( validatedValueType );
			constraintValidator = createAndInitializeValidator( constraintFactory, validatorClass );
			constraintValidatorCache.put( key, constraintValidator );
		}
		else {
			log.tracef( "Constraint validator %s found in cache.", constraintValidator );
		}
		return constraintValidator;
	}

	@SuppressWarnings("unchecked")
	private <V> ConstraintValidator<A, V> createAndInitializeValidator(ConstraintValidatorFactory constraintFactory, Class<? extends ConstraintValidator<?, ?>> validatorClass) {
		ConstraintValidator<A, V> constraintValidator;
		constraintValidator = (ConstraintValidator<A, V>) constraintFactory.getInstance(
				validatorClass
		);
		if ( constraintValidator == null ) {
			throw log.getConstraintFactoryMustNotReturnNullException( validatorClass.getName() );
		}
		initializeConstraint( descriptor, constraintValidator );
		return constraintValidator;
	}

	/**
	 * Runs the validator resolution algorithm.
	 *
	 * @param validatedValueType The type of the value to be validated (the type of the member/class the constraint was placed on).
	 *
	 * @return The class of a matching validator.
	 */
	private Class<? extends ConstraintValidator<?, ?>> findMatchingValidatorClass(Type validatedValueType) {
		Map<Type, Class<? extends ConstraintValidator<?, ?>>> availableValidatorTypes = TypeHelper.getValidatorsTypes(
				descriptor.getConstraintValidatorClasses()
		);
		Map<Type, Type> suitableTypeMap = newHashMap();

		if ( suitableTypeMap.containsKey( validatedValueType ) ) {
			return availableValidatorTypes.get( suitableTypeMap.get( validatedValueType ) );
		}

		List<Type> discoveredSuitableTypes = findSuitableValidatorTypes( validatedValueType, availableValidatorTypes );
		resolveAssignableTypes( discoveredSuitableTypes );
		verifyResolveWasUnique( validatedValueType, discoveredSuitableTypes );

		Type suitableType = discoveredSuitableTypes.get( 0 );
		suitableTypeMap.put( validatedValueType, suitableType );
		return availableValidatorTypes.get( suitableType );
	}

	private void verifyResolveWasUnique(Type valueClass, List<Type> assignableClasses) {
		if ( assignableClasses.size() == 0 ) {
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

	private List<Type> findSuitableValidatorTypes(Type type, Map<Type, Class<? extends ConstraintValidator<?, ?>>> availableValidatorTypes) {
		List<Type> determinedSuitableTypes = new ArrayList<Type>();
		for ( Type validatorType : availableValidatorTypes.keySet() ) {
			if ( TypeHelper.isAssignable( validatorType, type )
					&& !determinedSuitableTypes.contains( validatorType ) ) {
				determinedSuitableTypes.add( validatorType );
			}
		}
		return determinedSuitableTypes;
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

	private <V> void initializeConstraint(ConstraintDescriptor<A> descriptor, ConstraintValidator<A, V> constraintValidator) {
		try {
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToInitializeConstraintValidatorException( constraintValidator.getClass().getName(), e );
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintTree" );
		sb.append( "{ descriptor=" ).append( descriptor );
		sb.append( ", isRoot=" ).append( parent == null );
		sb.append( '}' );
		return sb.toString();
	}

	private static final class ConstraintValidatorCacheKey {
		private ConstraintValidatorFactory constraintValidatorFactory;
		private Type validatedType;

		private ConstraintValidatorCacheKey(ConstraintValidatorFactory constraintValidatorFactory, Type validatorType) {
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.validatedType = validatorType;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			ConstraintValidatorCacheKey that = (ConstraintValidatorCacheKey) o;

			if ( constraintValidatorFactory != null ? !constraintValidatorFactory.equals( that.constraintValidatorFactory ) : that.constraintValidatorFactory != null ) {
				return false;
			}
			if ( validatedType != null ? !validatedType.equals( that.validatedType ) : that.validatedType != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = constraintValidatorFactory != null ? constraintValidatorFactory.hashCode() : 0;
			result = 31 * result + ( validatedType != null ? validatedType.hashCode() : 0 );
			return result;
		}
	}

	private static final class CompositionResult {
		private boolean allTrue;
		private boolean atLeastOneTrue;

		CompositionResult(boolean allTrue, boolean atLeastOneTrue) {
			this.allTrue = allTrue;
			this.atLeastOneTrue = atLeastOneTrue;
		}

		public boolean isAllTrue() {
			return allTrue;
		}

		public boolean isAtLeastOneTrue() {
			return atLeastOneTrue;
		}

		public void setAllTrue(boolean allTrue) {
			this.allTrue = allTrue;
		}

		public void setAtLeastOneTrue(boolean atLeastOneTrue) {
			this.atLeastOneTrue = atLeastOneTrue;
		}
	}
}
