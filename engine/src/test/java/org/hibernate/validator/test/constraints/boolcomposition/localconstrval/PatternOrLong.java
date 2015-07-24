/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.OR;

/**
 * Test constraint for HV-390.
 *
 * Uses the same constraintValidator as SmallString, but does not negate the result.
 * Hence it tests that a string is long at least 9 characters (or that it matches the given pattern).
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */

@ConstraintComposition(OR)
@Pattern(regexp = "W{4}")
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { LongStringValidator.class })
public @interface PatternOrLong {
	String message() default "Both Pattern and LongString failed";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
