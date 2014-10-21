/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.constraints.ConstraintComposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.AND;

/**
 * Checks that a number is an allowed SSN
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
@Documented
@ConstraintComposition(AND)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ValidSSN
@Constraint(validatedBy = { })
@Blacklist
public @interface AllowedSSN {
	String message() default "Invalid or blacklisted social security number";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
