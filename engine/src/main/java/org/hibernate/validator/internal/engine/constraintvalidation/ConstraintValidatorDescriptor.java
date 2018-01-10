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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.cfg.context.ConstraintDefinitionContext.ValidationCallable;

/**
 * Represents a specific validator (either based on an implementation of {@link ConstraintValidator} or given as a
 * Lambda expression/method reference.
 *
 * @author Gunnar Morling
 */
public interface ConstraintValidatorDescriptor<A extends Annotation> {

	/**
	 * The implementation type of the represented validator.
	 */
	Class<? extends ConstraintValidator<A, ?>> getValidatorClass();

	/**
	 * The targets supported for validation by the represented validator.
	 */
	EnumSet<ValidationTarget> getValidationTargets();

	/**
	 * The data type validated by the represented validator (not the constraint annotation type).
	 */
	Type getValidatedType();

	/**
	 * Creates a new instance of the represented implementation type.
	 */
	ConstraintValidator<A, ?> newInstance(ConstraintValidatorFactory constraintValidatorFactory);

	static <A extends Annotation> ConstraintValidatorDescriptor<A> forClass(Class<? extends ConstraintValidator<A, ?>> validatorClass) {
		return new ClassBasedValidatorDescriptor<>( validatorClass );
	}

	static <A extends Annotation, T> ConstraintValidatorDescriptor<A> forLambda(Class<A> annotationType, Type validatedType, ValidationCallable<T> lambda) {
		return new LambdaBasedValidatorDescriptor<>( validatedType, lambda );
	}
}
