/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * Negation of NotBlank from the api
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */

@Constraint(validatedBy = { })
@ConstraintComposition(ALL_FALSE)
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@NotBlank
public @interface IsBlank {
	String message() default "Is Not Blank";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
