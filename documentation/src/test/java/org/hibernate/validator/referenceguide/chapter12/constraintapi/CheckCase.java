/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface CheckCase {

	String message() default "{org.hibernate.validator.referenceguide.chapter11.constraintapi.CheckCase.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	CaseMode value();

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		CheckCase[] value();
	}
}
