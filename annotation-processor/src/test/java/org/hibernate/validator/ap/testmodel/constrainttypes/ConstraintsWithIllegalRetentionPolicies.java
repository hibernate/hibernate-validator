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
