/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.constrainttypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

/**
 * @author Gunnar Morling
 */
public interface ConstraintsWithoutValidator {

	/**
	 * Compilation error expected as no validator is given.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { })
	public @interface ConstraintWithoutValidator {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected as a validator is given.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithValidator {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error as this is a composed constraint.
	 */
	@Size
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { })
	public @interface ComposedConstraintWithoutValidator {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

}
