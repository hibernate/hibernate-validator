/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.OR;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.ConstraintComposition;

/**
 * Test constraint for HV-390.
 *
 * @author Gerhard Petracek
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 */

@ConstraintComposition(OR)
@Pattern(regexp = "K")
@Size(min = 2, max = 10)
@ReportAsSingleViolation
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
public @interface PatternOrSize {
	String message() default "OR";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
