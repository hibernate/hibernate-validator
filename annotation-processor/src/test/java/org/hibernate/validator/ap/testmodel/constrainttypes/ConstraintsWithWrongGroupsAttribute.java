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
