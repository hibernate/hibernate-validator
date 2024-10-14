/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//spotless:off
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.crossparameter;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//end::include[]
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.Payload;

//spotless:on
//tag::include[]
@Constraint(validatedBy = {
		ScriptAssertObjectValidator.class,
		ScriptAssertParametersValidator.class
})
@Target({ TYPE, FIELD, PARAMETER, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ScriptAssert {

	String message() default "{org.hibernate.validator.referenceguide.chapter04." +
			"crossparameter.ScriptAssert.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	String script();

	ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;
}
//end::include[]
