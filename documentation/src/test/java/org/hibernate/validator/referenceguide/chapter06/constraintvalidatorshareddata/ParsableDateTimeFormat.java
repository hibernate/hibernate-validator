/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = ParsableDateTimeFormatValidator.class)
@Documented
public @interface ParsableDateTimeFormat {

	String dateFormat() default "dd/MM/yyyy";

	String message() default "{org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata.FutureString.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
