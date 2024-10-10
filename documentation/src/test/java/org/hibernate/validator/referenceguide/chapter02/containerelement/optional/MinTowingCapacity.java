/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter02.containerelement.optional;

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

@Documented
@Constraint(validatedBy = { MinTowingCapacity.MinTowingCapacityValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface MinTowingCapacity {
	long value();

	String message() default "{org.hibernate.validator.referenceguide.chapter02.containerelement.MinTowingCapacity.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class MinTowingCapacityValidator implements ConstraintValidator<MinTowingCapacity, Integer> {
		private long min;

		@Override
		public void initialize(MinTowingCapacity annotation) {
			min = annotation.value();
		}

		@Override
		public boolean isValid(Integer value, ConstraintValidatorContext context) {
			if ( value == null ) {
				return true;
			}

			return value >= min;
		}
	}
}
