/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.ap.testmodel.constrainttypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

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

	public static class SamplePayload implements Payload {
	}

}
