/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.packageprivateconstraint;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @author Gunnar Morling
 */
@Constraint(validatedBy = ValidAnimalNameValidator.class)
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@interface ValidAnimalName {

	String message() default "{org.hibernate.validator.test.internal.engine.privateconstraint.ValidAnimalName.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	String value();
}
