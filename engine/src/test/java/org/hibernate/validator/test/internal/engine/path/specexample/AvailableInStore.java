/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
@NotNull
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = AvailableInStore.Validator.class)
@ReportAsSingleViolation
public @interface AvailableInStore {

	Class<?>[] groups() default { };

	String message() default "Default error message";

	Class<? extends Payload>[] payload() default { };

	class Validator implements ConstraintValidator<AvailableInStore, Book> {

		@Override
		public boolean isValid(Book value, ConstraintValidatorContext constraintValidatorContext) {
			return false;
		}
	}
}
