/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = MyConstraintValidator.class)
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface MyConstraint {

	String message() default "MyConstraint is not valid";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	AdditionalConstraint[] additionalConstraints() default {};

	public @interface AdditionalConstraint {

		String message() default "AdditionalConstraint is not valid";

		String constraint();
	}
}
