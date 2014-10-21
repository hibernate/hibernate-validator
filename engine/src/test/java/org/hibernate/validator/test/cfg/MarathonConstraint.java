/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Hardy Ferentschik
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { MarathonConstraintValidator.class })
public @interface MarathonConstraint {
	String message() default "invalid name";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	int minRunner();
}


