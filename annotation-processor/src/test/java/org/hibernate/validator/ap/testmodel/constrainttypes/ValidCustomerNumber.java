/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.constrainttypes;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Constraint annotations are not allowed here, as ValidCustomerNumber isn't a
 * proper constraint type definition.
 */
@NotNull
@Size(min = 10, max = 10)
public @interface ValidCustomerNumber {
	String message() default "";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
