/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.constrainttarget;

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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Gunnar Morling
 */
@Documented
@Constraint(validatedBy = ComposedGenericAndCrossParameterConstraint.GenericValidator.class)
@Target({ METHOD, CONSTRUCTOR, TYPE, FIELD })
@Retention(RUNTIME)
@GenericAndCrossParameterConstraint
public @interface ComposedGenericAndCrossParameterConstraint {
	String message() default "default message";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;

	@SupportedValidationTarget(value = { ValidationTarget.ANNOTATED_ELEMENT, ValidationTarget.PARAMETERS })
	class GenericValidator
			implements ConstraintValidator<ComposedGenericAndCrossParameterConstraint, Object> {

		@Override
		public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
			return false;
		}
	}
}
