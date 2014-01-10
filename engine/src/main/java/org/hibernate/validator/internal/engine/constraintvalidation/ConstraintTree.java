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
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;
import static org.hibernate.validator.constraints.CompositionType.AND;
import static org.hibernate.validator.constraints.CompositionType.OR;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

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

	private final ConstraintTree<?> parent;
	private final List<ConstraintTree<?>> children;

	/**
	 * The constraint descriptor for the constraint represented by this constraint tree.
	 */
	private final ConstraintDescriptorImpl<A> descriptor;

	public ConstraintTree(ConstraintDescriptorImpl<A> descriptor) {
		this( descriptor, null );
	}

	private ConstraintTree(ConstraintDescriptorImpl<A> descriptor, ConstraintTree<?> parent) {
		this.parent = parent;
		this.descriptor = descriptor;

		final Set<ConstraintDescriptorImpl<?>> composingConstraints = descriptor.getComposingConstraintImpls();
		children = newArrayList( composingConstraints.size() );

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

	public final <T> boolean validateConstraints(ValidationContext<T> executionContext, ValueContext<?, ?> valueContext) {
		Set<ConstraintViolation<T>> constraintViolations = newHashSet();
		validateConstraints( executionContext, valueContext, constraintViolations );
		if ( !constraintViolations.isEmpty() ) {
			executionContext.addConstraintFailures( constraintViolations );
			return false;
		}
		return true;
	}

	private <T, V> void validateConstraints(ValidationContext<T> executionContext,
											ValueContext<?, V> valueContext,
											Set<ConstraintViolation<T>> constraintViolations) {
		CompositionResult compositionResult = validateComposingConstraints(
				executionContext, valueContext, constraintViolations
		);

		Set<ConstraintViolation<T>> localViolations;

		// After all children are validated the actual ConstraintValidator of the constraint itself is executed
		if ( mainConstraintNeedsEvaluation( executionContext, constraintViolations ) ) {

			if ( log.isTraceEnabled() ) {
				log.tracef(
						"Validating value %s against constraint defined by %s.",
						valueContext.getCurrentValidatedValue(),
						descriptor
				);
			}

			// create a constraint validator context
			ConstraintValidatorContextImpl constraintValidatorContext = new ConstraintValidatorContextImpl(
					executionContext.getParameterNames(), valueContext.getPropertyPath(), descriptor
			);

			// get the initialized validator
			ConstraintValidator<A, V> validator = executionContext.getConstraintValidatorManager()
					.getInitializedValidator(
							valueContext.getTypeOfAnnotatedElement(),
							descriptor,
							executionContext.getConstraintValidatorFactory()
					);

			// validate
			localViolations = validateSingleConstraint(
					executionContext,
					valueContext,
					constraintValidatorContext,
					validator
			);

			// We re-evaluate the boolean composition by taking into consideration also the violations
			// from the local constraintValidator
			if ( localViolations.isEmpty() ) {
				compositionResult.setAtLeastOneTrue( true );
			}
			else {
				compositionResult.setAllTrue( false );
			}
		}
		else {
			localViolations = Collections.emptySet();
		}

		if ( !passesCompositionTypeRequirement( constraintViolations, compositionResult ) ) {
			prepareFinalConstraintViolations(
					executionContext, valueContext, constraintViolations, localViolations
			);
		}
	}

	private <T> boolean mainConstraintNeedsEvaluation(ValidationContext<T> executionContext, Set<ConstraintViolation<T>> constraintViolations) {
		// there is no validator for the main constraints
		if ( descriptor.getMatchingConstraintValidatorClasses().isEmpty() ) {
			return false;
		}

		// report as single violation and there is already a violation
		if ( descriptor.isReportAsSingleViolation() && descriptor.getCompositionType() == AND && !constraintViolations.isEmpty() ) {
			return false;
		}

		// explicit fail fast mode
		if ( executionContext.isFailFastModeEnabled() && !constraintViolations.isEmpty() ) {
			return false;
		}

		return true;
	}

	/**
	 * Before the final constraint violations can be reported back we need to check whether we have a composing
	 * constraint whose result should be reported as single violation.
	 *
	 * @param executionContext meta data about top level validation
	 * @param valueContext meta data for currently validated value
	 * @param constraintViolations used to accumulate constraint violations
	 * @param localViolations set of constraint violations of top level constraint
	 */
	private <T> void prepareFinalConstraintViolations(ValidationContext<T> executionContext, ValueContext<?, ?> valueContext, Set<ConstraintViolation<T>> constraintViolations, Set<ConstraintViolation<T>> localViolations) {
		if ( reportAsSingleViolation() ) {
			// We clear the current violations list anyway
			constraintViolations.clear();

			// But then we need to distinguish whether the local ConstraintValidator has reported
			// violations or not (or if there is no local ConstraintValidator at all).
			// If not we create a violation
			// using the error message in the annotation declaration at top level.
			if ( localViolations.isEmpty() ) {
				final String message = (String) getDescriptor().getAttributes().get( "message" );
				ConstraintViolationCreationContext constraintViolationCreationContext = new ConstraintViolationCreationContext( message, valueContext.getPropertyPath() );
				ConstraintViolation<T> violation = executionContext.createConstraintViolation(
						valueContext, constraintViolationCreationContext, descriptor
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
		constraintViolations.addAll( localViolations );
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
	private <T> CompositionResult validateComposingConstraints(ValidationContext<T> executionContext,
															   ValueContext<?, ?> valueContext,
															   Set<ConstraintViolation<T>> constraintViolations) {
		CompositionResult compositionResult = new CompositionResult( true, false );
		for ( ConstraintTree<?> tree : getChildren() ) {
			Set<ConstraintViolation<T>> tmpViolations = newHashSet();
			tree.validateConstraints( executionContext, valueContext, tmpViolations );
			constraintViolations.addAll( tmpViolations );

			if ( tmpViolations.isEmpty() ) {
				compositionResult.setAtLeastOneTrue( true );
				// no need to further validate constraints, because at least one validation passed
				if ( descriptor.getCompositionType() == OR ) {
					break;
				}
			}
			else {
				compositionResult.setAllTrue( false );
				if ( descriptor.getCompositionType() == AND
						&& ( executionContext.isFailFastModeEnabled() || descriptor.isReportAsSingleViolation() ) ) {
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

	private <T, V> Set<ConstraintViolation<T>> validateSingleConstraint(ValidationContext<T> executionContext,
																		ValueContext<?, ?> valueContext,
																		ConstraintValidatorContextImpl constraintValidatorContext,
																		ConstraintValidator<A, V> validator) {
		boolean isValid;
		try {
			@SuppressWarnings("unchecked")
			V validatedValue = (V) valueContext.getCurrentValidatedValue();
			isValid = validator.isValid( validatedValue, constraintValidatorContext );
		}
		catch ( RuntimeException e ) {
			throw log.getExceptionDuringIsValidCallException( e );
		}
		if ( !isValid ) {
			//We do not add these violations yet, since we don't know how they are
			//going to influence the final boolean evaluation
			return executionContext.createConstraintViolations(
					valueContext, constraintValidatorContext
			);
		}
		return Collections.emptySet();
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintTree" );
		sb.append( "{ descriptor=" ).append( descriptor );
		sb.append( ", isRoot=" ).append( parent == null );
		sb.append( '}' );
		return sb.toString();
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
