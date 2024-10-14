/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integrationtest.java.module.no.el.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import org.hibernate.validator.integrationtest.java.module.no.el.model.Car;

@Documented
@Constraint(validatedBy = { CarConstraint.Validator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface CarConstraint {
	String message() default "CarConstraint:message";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class Validator implements ConstraintValidator<CarConstraint, Car> {

		@Override
		public void initialize(CarConstraint annotation) {
		}

		@Override
		public boolean isValid(Car value, ConstraintValidatorContext context) {
			if ( value == null ) {
				return true;
			}

			return false;
		}
	}
}
