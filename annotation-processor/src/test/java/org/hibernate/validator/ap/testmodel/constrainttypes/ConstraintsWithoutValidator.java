/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
