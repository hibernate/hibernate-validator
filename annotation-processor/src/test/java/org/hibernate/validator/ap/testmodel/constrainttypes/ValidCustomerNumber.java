/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
