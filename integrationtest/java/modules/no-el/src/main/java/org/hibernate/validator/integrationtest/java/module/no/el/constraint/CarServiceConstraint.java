/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface CarServiceConstraint {
	String message() default "CarServiceConstraint:message";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class Validator implements ConstraintValidator<CarServiceConstraint, Car> {

		@Override
		public void initialize(CarServiceConstraint annotation) {
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
