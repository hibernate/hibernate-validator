/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;


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
	 * through {@link javax.validation.Constraint#validatedBy()} or the validation engine defaults) should
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
}
