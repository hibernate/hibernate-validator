/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.Incubating;


/**
 * Constraint mapping creational context representing a constraint (i.e. annotation type). Allows to define which
 * validators should validate this constraint.
 *
 * @param <A> The annotation type represented by this context.
 *
 * @author Yoann Rodiere
 */
public interface ConstraintDefinitionContext<A extends Annotation> extends ConstraintMappingTarget {

	/**
	 * Specifies whether validators already mapped to this constraint (i.e. defined in the annotation declaration
	 * through {@link jakarta.validation.Constraint#validatedBy()} or the validation engine defaults) should
	 * be included or not.
	 *
	 * @param includeExistingValidators Whether or not to use already-mapped validators when validating this constraint.
	 * @return This context for method chaining.
	 */
	ConstraintDefinitionContext<A> includeExistingValidators(boolean includeExistingValidators);

	/**
	 * Adds a new validator to validate this constraint.
	 *
	 * @param validator The validator to add.
	 *
	 * @return This context for method chaining.
	 */
	ConstraintDefinitionContext<A> validatedBy(Class<? extends ConstraintValidator<A, ?>> validator);

	/**
	 * Allows to configure a validation implementation using a Lambda expression or method reference. Useful for simple
	 * validations without the need for accessing constraint properties or customization of error messages etc.
	 * <p>
	 *
	 * @param type The type of the value to validate
	 * @return This context for method chaining
	 */
	@Incubating
	<T> ConstraintValidatorDefinitionContext<A, T> validateType(Class<T> type);

	/**
	 * Allows to specify a validation implementation for the given constraint and data type using a Lambda expression or
	 * method reference.
	 */
	@Incubating
	interface ConstraintValidatorDefinitionContext<A extends Annotation, T> {

		/**
		 * Applies the given Lambda expression or referenced method to values to be validated. It is guaranteed that
		 * {@code null} is never passed to these expressions or methods.
		 */
		ConstraintDefinitionContext<A> with(ValidationCallable<T> vc);
	}

	/**
	 * Callable implementing a validation routine. Usually given as method reference or Lambda expression.
	 */
	@FunctionalInterface
	@Incubating
	interface ValidationCallable<T> {
		boolean isValid(T object);
	}
}
