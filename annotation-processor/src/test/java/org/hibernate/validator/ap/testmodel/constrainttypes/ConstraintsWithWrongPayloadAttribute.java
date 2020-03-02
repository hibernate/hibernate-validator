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
public interface ConstraintsWithWrongPayloadAttribute {

	/**
	 * Compilation error expected due to missing payload attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithoutPayloadParameter {

		String message() default "";

		Class<?>[] groups() default { };
	}

	/**
	 * Compilation error expected due to payload attribute of wrong type.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithWrongType1 {

		String message() default "";

		Class<?>[] groups() default { };

		int[] payload() default { };

	}

	/**
	 * Compilation error expected due to payload attribute of non-array class type.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithWrongType2 {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload> payload();

	}

	/**
	 * Compilation error expected due to payload attribute with wrong extends bound.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithWrongExtendsBound {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Long>[] payload() default { };

	}

	/**
	 * Compilation error expected due to payload attribute without extend bound.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithoutExtendsBound {

		String message() default "";

		Class<?>[] groups() default { };

		Class<?>[] payload() default { };

	}

	/**
	 * Compilation error expected due to payload attribute with super bound.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithSuperBound {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? super Payload>[] payload() default { };

	}

	/**
	 * Compilation error expected due missing default value for payload attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithoutDefaultValue {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload();

	}

	/**
	 * Compilation error expected due to non-empty default value for payload attribute.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithPayloadParameterWithNonEmptyDefaultValue {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { SamplePayload.class };

	}

	/**
	 * No compilation error expected.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { DummyValidator.class })
	public @interface ConstraintWithCorrectPayloadParameter {

		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

	}

	class SamplePayload implements Payload {
	}

}
