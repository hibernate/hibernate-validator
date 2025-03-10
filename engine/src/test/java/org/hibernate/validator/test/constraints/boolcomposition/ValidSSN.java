/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.OR;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * Checks that a number is a norwegian temporary or permanent social security number
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
@Documented
@ConstraintComposition(OR)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@SSN
@Constraint(validatedBy = { })
@TemporarySSN
public @interface ValidSSN {
	String message() default "Not a valid social security number";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
