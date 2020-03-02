/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.constrainttypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author Gunnar Morling
 */
public interface ConstraintsWithIllegalTargets {


	/**
	 * Compilation error expected due to none supported target type being given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ LOCAL_VARIABLE })
	public @interface ConstraintWithWrongTarget {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected as no supported target type is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ })
	public @interface ConstraintWithEmptyTarget {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected, as not specifying @Target allows this constraint for all element types.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConstraintWithDefaultTarget {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected, as supported target type FIELD is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ FIELD })
	public @interface ConstraintWithAllowedTargetField {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected, as supported target type METHOD is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ METHOD })
	public @interface ConstraintWithAllowedTargetMethod {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected, as supported target type TYPE is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ TYPE })
	public @interface ConstraintWithAllowedTargetType {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	/**
	 * No compilation error expected, as supported target type ANNOTATION_TYPE is given.
	 */
	@Constraint(validatedBy = { DummyValidator.class })
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ANNOTATION_TYPE })
	public @interface ConstraintWithAllowedTargetAnnotationType {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

}
