/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;
import static org.hibernate.validator.constraints.CompositionType.AND;
import static org.hibernate.validator.constraints.CompositionType.OR;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.constraints.CompositionType;
import org.hibernate.validator.internal.engine.validationcontext.ValidationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A constraint tree for composing constraints. Has children corresponding to the composed constraints.
 *
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ComposingConstraintTree<B extends Annotation> extends ConstraintTree<B> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	private final List<ConstraintTree<?>> children;

	public ComposingConstraintTree(ConstraintValidatorManager constraintValidatorManager, ConstraintDescriptorImpl<B> descriptor, Type validatedValueType) {
		super( constraintValidatorManager, descriptor, validatedValueType );
		this.children = descriptor.getComposingConstraintImpls().stream()
				.map( desc -> createConstraintTree( constraintValidatorManager, desc ) )
				.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) );
	}

	private <U extends Annotation> ConstraintTree<U> createConstraintTree(ConstraintValidatorManager constraintValidatorManager, ConstraintDescriptorImpl<U> composingDescriptor) {
		if ( composingDescriptor.getComposingConstraintImpls().isEmpty() ) {
			return new SimpleConstraintTree<>( constraintValidatorManager, composingDescriptor, getValidatedValueType() );
		}
		else {
			return new ComposingConstraintTree<>( constraintValidatorManager, composingDescriptor, getValidatedValueType() );
		}
	}

	@Override
	protected void validateConstraints(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts) {
		CompositionResult compositionResult = validateComposingConstraints(
				validationContext, valueContext, violatedConstraintValidatorContexts
		);

		Optional<ConstraintValidatorContextImpl> violatedLocalConstraintValidatorContext;

		// After all children are validated the actual ConstraintValidator of the constraint itself is executed
		if ( mainConstraintNeedsEvaluation( validationContext, violatedConstraintValidatorContexts ) ) {

			if ( LOG.isTraceEnabled() ) {
				LOG.tracef(
						"Validating value %s against constraint defined by %s.",
						valueContext.getCurrentValidatedValue(),
						descriptor
				);
			}

			// find the right constraint validator
			ConstraintValidator<B, ?> validator = getInitializedConstraintValidator( validationContext, valueContext );

			// create a constraint validator context
			ConstraintValidatorContextImpl constraintValidatorContext = validationContext.createConstraintValidatorContextFor(
					descriptor, valueContext.getPropertyPath()
			);

			// validate
			violatedLocalConstraintValidatorContext = validateSingleConstraint(
					valueContext,
					constraintValidatorContext,
					validator
			);

			// We re-evaluate the boolean composition by taking into consideration also the violations
			// from the local constraintValidator
			if ( !violatedLocalConstraintValidatorContext.isPresent() ) {
				compositionResult.setAtLeastOneTrue( true );
			}
			else {
				compositionResult.setAllTrue( false );
			}
		}
		else {
			violatedLocalConstraintValidatorContext = Optional.empty();
		}

		if ( !passesCompositionTypeRequirement( violatedConstraintValidatorContexts, compositionResult ) ) {
			prepareFinalConstraintViolations(
					validationContext, valueContext, violatedConstraintValidatorContexts, violatedLocalConstraintValidatorContext
			);
		}
	}

	private boolean mainConstraintNeedsEvaluation(ValidationContext<?> validationContext,
			Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts) {
		// we are dealing with a composing constraint with no validator for the main constraint
		if ( !descriptor.getComposingConstraints().isEmpty() && descriptor.getMatchingConstraintValidatorDescriptors().isEmpty() ) {
			return false;
		}

		if ( violatedConstraintValidatorContexts.isEmpty() ) {
			return true;
		}

		// report as single violation and there is already a violation
		if ( descriptor.isReportAsSingleViolation() && descriptor.getCompositionType() == AND ) {
			return false;
		}

		// explicit fail fast mode
		if ( validationContext.isFailFastModeEnabled() ) {
			return false;
		}

		return true;
	}

	/**
	 * Before the final constraint violations can be reported back we need to check whether we have a composing
	 * constraint whose result should be reported as single violation.
	 *
	 * @param validationContext meta data about top level validation
	 * @param valueContext meta data for currently validated value
	 * @param violatedConstraintValidatorContexts used to accumulate constraint validator contexts that cause constraint violations
	 * @param localConstraintValidatorContext an optional of constraint violations of top level constraint
	 */
	private void prepareFinalConstraintViolations(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts,
			Optional<ConstraintValidatorContextImpl> localConstraintValidatorContext) {
		if ( reportAsSingleViolation() ) {
			// We clear the current violations list anyway
			violatedConstraintValidatorContexts.clear();

			// But then we need to distinguish whether the local ConstraintValidator has reported
			// violations or not (or if there is no local ConstraintValidator at all).
			// If not we create a violation
			// using the error message in the annotation declaration at top level.
			if ( !localConstraintValidatorContext.isPresent() ) {
				violatedConstraintValidatorContexts.add(
						validationContext.createConstraintValidatorContextFor(
								descriptor, valueContext.getPropertyPath()
						)
				);
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
		if ( localConstraintValidatorContext.isPresent() ) {
			violatedConstraintValidatorContexts.add( localConstraintValidatorContext.get() );
		}
	}

	/**
	 * Validates all composing constraints recursively.
	 *
	 * @param validationContext Meta data about top level validation
	 * @param valueContext Meta data for currently validated value
	 * @param violatedConstraintValidatorContexts Used to accumulate constraint validator contexts that cause constraint violations
	 *
	 * @return Returns an instance of {@code CompositionResult} relevant for boolean composition of constraints
	 */
	private CompositionResult validateComposingConstraints(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts) {
		CompositionResult compositionResult = new CompositionResult( true, false );
		for ( ConstraintTree<?> tree : children ) {
			List<ConstraintValidatorContextImpl> tmpConstraintValidatorContexts = new ArrayList<>( 5 );
			tree.validateConstraints( validationContext, valueContext, tmpConstraintValidatorContexts );
			violatedConstraintValidatorContexts.addAll( tmpConstraintValidatorContexts );

			if ( tmpConstraintValidatorContexts.isEmpty() ) {
				compositionResult.setAtLeastOneTrue( true );
				// no need to further validate constraints, because at least one validation passed
				if ( descriptor.getCompositionType() == OR ) {
					break;
				}
			}
			else {
				compositionResult.setAllTrue( false );
				if ( descriptor.getCompositionType() == AND
						&& ( validationContext.isFailFastModeEnabled() || descriptor.isReportAsSingleViolation() ) ) {
					break;
				}
			}
		}
		return compositionResult;
	}

	private boolean passesCompositionTypeRequirement(Collection<?> constraintViolations, CompositionResult compositionResult) {
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

	/**
	 * @return {@code} true if the current constraint should be reported as single violation, {@code false otherwise}.
	 * 		When using negation, we only report the single top-level violation, as
	 * 		it is hard, especially for ALL_FALSE to give meaningful reports
	 */
	private boolean reportAsSingleViolation() {
		return getDescriptor().isReportAsSingleViolation()
				|| getDescriptor().getCompositionType() == ALL_FALSE;
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
