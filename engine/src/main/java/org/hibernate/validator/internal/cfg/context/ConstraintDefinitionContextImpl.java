/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * Constraint definition context which allows to configure the validators to be used for a constraint's validation.
 *
 * @param <A> The constraint (i.e. annotation type) represented by this context.
 *
 * @author Yoann Rodiere
 */
class ConstraintDefinitionContextImpl<A extends Annotation>
		extends ConstraintContextImplBase
		implements ConstraintDefinitionContext<A> {

	private final Class<A> annotationType;

	private boolean includeExistingValidators = true;

	private final Set<ConstraintValidatorDescriptor<A>> validatorDescriptors = new HashSet<>();

	ConstraintDefinitionContextImpl(DefaultConstraintMapping mapping, Class<A> annotationType) {
		super( mapping );
		this.annotationType = annotationType;
	}

	@Override
	public ConstraintDefinitionContext<A> includeExistingValidators(boolean includeExistingValidators) {
		this.includeExistingValidators = includeExistingValidators;
		return this;
	}

	@Override
	public ConstraintDefinitionContext<A> validatedBy(Class<? extends ConstraintValidator<A, ?>> validator) {
		validatorDescriptors.add( ConstraintValidatorDescriptor.forClass( validator, this.annotationType ) );
		return this;
	}

	@Override
	public <T> ConstraintDefinitionContext.ConstraintValidatorDefinitionContext<A, T> validateType(Class<T> type) {
		return new ConstraintValidatorDefinitionContextImpl<>( type );
	}

	@SuppressWarnings("unchecked")
	ConstraintDefinitionContribution<A> build() {
		return new ConstraintDefinitionContribution<>(
				annotationType,
				CollectionHelper.newArrayList( validatorDescriptors ),
				includeExistingValidators );
	}

	private class ConstraintValidatorDefinitionContextImpl<T> implements ConstraintDefinitionContext.ConstraintValidatorDefinitionContext<A, T> {

		private final Class<T> type;

		public ConstraintValidatorDefinitionContextImpl(Class<T> type) {
			this.type = type;
		}

		@Override
		public ConstraintDefinitionContext<A> with(ConstraintDefinitionContext.ValidationCallable<T> vc) {
			validatorDescriptors.add( ConstraintValidatorDescriptor.forLambda( annotationType, type, vc ) );

			return ConstraintDefinitionContextImpl.this;
		}
	}
}
