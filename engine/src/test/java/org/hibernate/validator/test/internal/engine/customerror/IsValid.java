/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.customerror;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Hardy Ferentschik
 */
@NotNull
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = IsValidValidator.class)
@ReportAsSingleViolation
public @interface IsValid {
	Class<?>[] groups() default { };

	String message() default "Default error message";

	Class<? extends Payload>[] payload() default { };
}
