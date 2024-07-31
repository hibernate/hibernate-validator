/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @author Hardy Ferentschik
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface MustNotMatch {
	String message() default "{org.hibernate.validator.test.constraintvalidator.MustNotMatch.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	String value();
}
