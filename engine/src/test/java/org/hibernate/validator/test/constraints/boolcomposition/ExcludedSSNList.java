/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.List;

import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * Excluded SSN numbers
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */

@ConstraintComposition(ALL_FALSE)
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@List({
		@Pattern(regexp = "12345678901"),
		@Pattern(regexp = "98765678765"),
		@Pattern(regexp = "55555555555"),
		@Pattern(regexp = "123456")
})
public @interface ExcludedSSNList {
	String message() default "Excluded SSN";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
