/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ConstraintWithPrivateValidator.ConstraintWithPrivateValidatorValidator.class })
public @interface ConstraintWithPrivateValidator {
	String message() default "Invalid";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class ConstraintWithPrivateValidatorValidator implements ConstraintValidator<ConstraintWithPrivateValidator, Object> {
		private ConstraintWithPrivateValidatorValidator() {
		}

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	}

}
