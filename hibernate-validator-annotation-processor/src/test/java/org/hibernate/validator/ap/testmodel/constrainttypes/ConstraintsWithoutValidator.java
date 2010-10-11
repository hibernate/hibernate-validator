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
import javax.validation.constraints.Size;

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
