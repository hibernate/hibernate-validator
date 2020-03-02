/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.crossparameter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * @author Hardy Ferentschik
 */
@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CrossParameterValidator1.class, CrossParameterValidator2.class })
@Documented
public @interface InvalidCrossParameterConstraint {
	String message() default "{ConsistentDateParameters.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
