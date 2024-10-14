/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

import java.lang.annotation.Retention;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * Test mode for HV-390.
 *
 * Composed annotation with both boolean operators and local constraintValidator.
 * It checks in a very complicated way whether a string is strictly shorter than 11 chars.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
@Constraint(validatedBy = SmallStringValidator.class)
@ConstraintComposition(ALL_FALSE)
@Size(min = 10, max = 10)
@Retention(RUNTIME)
public @interface SmallString {
	String message() default "Not a small string";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
