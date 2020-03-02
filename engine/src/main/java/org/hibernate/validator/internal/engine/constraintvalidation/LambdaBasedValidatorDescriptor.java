/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.EnumSet;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.cfg.context.ConstraintDefinitionContext.ValidationCallable;

/**
 * Represents a constraint validator based on a Lambda expression.
 *
 * @author Gunnar Morling
 */
class LambdaBasedValidatorDescriptor<A extends Annotation> implements ConstraintValidatorDescriptor<A> {

	private static final long serialVersionUID = 5129757824081595723L;

	private final Type validatedType;
	private final ValidationCallable<?> lambda;

	public LambdaBasedValidatorDescriptor(Type validatedType, ValidationCallable<?> lambda) {
		this.validatedType = validatedType;
		this.lambda = lambda;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ConstraintValidator<A, ?>> getValidatorClass() {
		Class<?> clazz = LambdaExecutor.class;
		return (Class<? extends ConstraintValidator<A, ?>>) clazz;
	}

	@Override
	public EnumSet<ValidationTarget> getValidationTargets() {
		return EnumSet.of( ValidationTarget.ANNOTATED_ELEMENT );
	}

	@Override
	public Type getValidatedType() {
		return validatedType;
	}

	@Override
	public ConstraintValidator<A, ?> newInstance(ConstraintValidatorFactory constraintValidatorFactory) {
		return new LambdaExecutor<>( lambda );
	}

	private static class LambdaExecutor<A extends Annotation, T> implements ConstraintValidator<A, T> {

		private final ValidationCallable<T> lambda;

		public LambdaExecutor(ValidationCallable<T> lambda) {
			this.lambda = lambda;
		}

		@Override
		public boolean isValid(T value, ConstraintValidatorContext context) {
			return lambda.isValid( value );
		}
	}
}
