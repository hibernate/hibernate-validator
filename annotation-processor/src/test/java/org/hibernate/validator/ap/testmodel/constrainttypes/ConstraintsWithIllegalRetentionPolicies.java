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

/**
 * @author Gunnar Morling
 */
public interface ConstraintsWithIllegalRetentionPolicies {

	/**
	 * Compilation error expected as wrong retention policy is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.CLASS)
	public @interface ConstraintWithWrongRetentionPolicy {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected as no retention policy is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithoutRetentionPolicy {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected as correct retention policy is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstraintWithCorrectRetentionPolicy {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

}
