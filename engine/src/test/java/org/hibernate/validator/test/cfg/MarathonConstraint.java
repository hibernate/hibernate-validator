/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @author Hardy Ferentschik
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { MarathonConstraintValidator.class })
public @interface MarathonConstraint {
	String message() default "invalid name";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	int minRunner();
}
