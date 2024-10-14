/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraintvalidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = MustNotMatchValidator.class)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface TypeMismatchConstraint {
	String message() default "{org.hibernate.validator.test.constraintvalidator.TypeMismatchConstraint.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
