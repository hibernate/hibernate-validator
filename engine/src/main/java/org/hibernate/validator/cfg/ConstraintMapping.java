/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;

import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;

/**
 * Represents a constraint mapping configured via the programmatic API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Yoann Rodiere
 */
public interface ConstraintMapping {
	/**
	 * Starts defining constraints on the specified bean class. Each bean class may only be configured once within all
	 * constraint mappings used for configuring one validator factory.
	 *
	 * @param <C> The type to be configured.
	 * @param beanClass The bean class on which to define constraints. All constraints defined after calling this method
	 * are added to the bean of the type {@code beanClass} until the next call of {@code type} or {@code annotation}.
	 *
	 * @return Instance allowing for defining constraints on the specified class.
	 */
	<C> TypeConstraintMappingContext<C> type(Class<C> beanClass);

	/**
	 * Starts defining {@link jakarta.validation.ConstraintValidator}s to be executed for the specified constraint (i.e. annotation class).
	 * Each constraint may only be configured once within all constraint mappings used for configuring one validator
	 * factory.
	 *
	 * @param <A> The annotation type to be configured.
	 * @param annotationClass The annotation class on which to define the validators. This type must be an
	 * {@code @interface} annotated with {@code jakarta.validation.Constraint}. All validators defined after calling
	 * this method are added to the annotation of the type {@code annotationClass} until the next call
	 * of {@code type} or {@code annotation}.
	 *
	 * @return Instance allowing for defining validators to be executed for the specified constraint.
	 * @since 5.3
	 */
	<A extends Annotation> ConstraintDefinitionContext<A> constraintDefinition(Class<A> annotationClass);
}
