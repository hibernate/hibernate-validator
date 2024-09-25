/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter10;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { LuggageCountMatchesPassengerCount.Validator.class })
@Documented
public @interface LuggageCountMatchesPassengerCount {

	String message() default "{org.hibernate.validator.referenceguide.chapter08.LuggageCountMatchesPassengerCount.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;

	int piecesOfLuggagePerPassenger() default 1;

	@SupportedValidationTarget({
			ValidationTarget.PARAMETERS,
			ValidationTarget.ANNOTATED_ELEMENT
	})
	class Validator
			implements ConstraintValidator<LuggageCountMatchesPassengerCount, Object[]> {

		@Override
		public void initialize(LuggageCountMatchesPassengerCount constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object[] value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
