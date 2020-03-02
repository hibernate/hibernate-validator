/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.validationtarget;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.test.constraints.composition.validationtarget.AlwaysFailingConstraint.AlwaysFailingConstraintValidator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Gunnar Morling
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AlwaysFailingConstraintValidator.class)
@ReportAsSingleViolation
public @interface AlwaysFailingConstraint {

	String message() default "{org.hibernate.validator.test.constraints.composition.validationtarget.CustomComposingConstraint.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;

	@SupportedValidationTarget({ ValidationTarget.ANNOTATED_ELEMENT, ValidationTarget.PARAMETERS })
	class AlwaysFailingConstraintValidator implements ConstraintValidator<AlwaysFailingConstraint, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
