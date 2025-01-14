/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.internal.constraintvalidators.bv.OneOfValidator;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {

	String[] allowedValues() default { };

	Class<? extends Enum<?>> enumClass() default DefaultEnum.class;

	boolean ignoreCase() default false;

	String message() default "must be one of {allowedValues} or is an invalid enum";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	enum DefaultEnum {
	}
}
