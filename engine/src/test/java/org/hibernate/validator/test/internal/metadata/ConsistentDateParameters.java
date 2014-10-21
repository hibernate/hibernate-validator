/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Cross-parameter constraint that checks that two date parameters are in the
 * correct order. Applies to methods with the signature
 * {@code methodName(DateMidnight start, DateMidnight end)}.
 *
 * @author Gunnar Morling
 */
@Target({ METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConsistentDateParametersValidator.class)
@Documented
public @interface ConsistentDateParameters {
	String message() default "{ConsistentDateParameters.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
