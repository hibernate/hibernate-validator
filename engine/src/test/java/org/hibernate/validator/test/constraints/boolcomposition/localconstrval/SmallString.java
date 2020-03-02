/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import java.lang.annotation.Retention;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.ConstraintComposition;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

/**
 * Test mode for HV-390.
 *
 * Composed annotation with both boolean operators and local constraintValidator.
 * It checks in a very complicated way whether a string is strictly shorter than 11 chars.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
@Constraint(validatedBy = SmallStringValidator.class)
@ConstraintComposition(ALL_FALSE)
@Size(min = 10, max = 10)
@Retention(RUNTIME)
public @interface SmallString {
	String message() default "Not a small string";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
