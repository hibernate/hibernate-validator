/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

import java.lang.annotation.Annotation;

/**
 * Facet of a constraint definition creational context which allows to select the constraint (annotation type) to
 * which the next operations shall apply.
 *
 * @author Yoann Rodiere
 */
public interface ConstraintDefinitionTarget {

	/**
	 * Selects the constraint (i.e. annotation type) to which the next operations shall apply. A given constraint
	 * may only be configured once.
	 *
	 * @param <A> The annotation type to select.
	 * @param annotationType The annotation type to select. This type must be an {@code @interface} annotated with
	 * {@code jakarta.validation.Constraint}.
	 *
	 * @return A creational context representing the selected constraint.
	 */
	<A extends Annotation> ConstraintDefinitionContext<A> constraintDefinition(Class<A> annotationType);
}
