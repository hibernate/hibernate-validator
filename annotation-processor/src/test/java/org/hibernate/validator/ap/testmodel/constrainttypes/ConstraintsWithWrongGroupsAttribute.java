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
public interface ConstraintsWithWrongGroupsAttribute {

	/**
	 * Compilation error expected due to missing groups attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithoutGroupsParameter {

		String message() default "";

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due to groups attribute of wrong type.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithWrongType1 {

		String message() default "";

		int[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due to groups attribute of non-array class type.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithWrongType2 {

		String message() default "";

		Class<?> groups();

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due to groups attribute with super bound.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithSuperBound {

		String message() default "";

		Class<? super Long>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due to groups attribute with extends bound.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithExtendsBound {

		String message() default "";

		Class<? extends Long>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due missing default value for groups attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithoutDefaultValue {

		String message() default "";

		Class<?>[] groups();

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due to non-empty default value for groups attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithGroupsParameterWithNonEmptyDefaultValue {

		String message() default "";

		Class<?>[] groups() default { Object.class };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithCorrectGroupsParameter {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

}
