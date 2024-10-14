/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.AND;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Boolean operator that is applied to all constraints of a composing constraint annotation.
 * <p>
 * A composed constraint annotation can define a boolean combination of the constraints composing it,
 * by using {@code @ConstraintComposition}.
 * </p>
 * @author Dag Hovland
 * @author Federico Mancini
 */
@Documented
@Target({ ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ConstraintComposition {
	/**
	 * The value of this element specifies the boolean operator,
	 * namely disjunction (OR), negation of the conjunction (ALL_FALSE),
	 * or, the default, simple conjunction (AND).
	 *
	 * @return the {@code CompositionType} value
	 */
	CompositionType value() default AND;
}
