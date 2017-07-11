/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

/**
 * @author Gunnar Morling
 */
@Target(METHOD)
@Retention(RUNTIME)
@Constraint(validatedBy = OldAndNewPasswordsDifferent.Validator.class)
@ReportAsSingleViolation
public @interface OldAndNewPasswordsDifferent {

	Class<?>[] groups() default { };

	String message() default "Default error message";

	Class<? extends Payload>[] payload() default { };

	@SupportedValidationTarget(ValidationTarget.PARAMETERS)
	class Validator implements ConstraintValidator<OldAndNewPasswordsDifferent, Object[]> {

		@Override
		public boolean isValid(Object[] args, ConstraintValidatorContext constraintValidatorContext) {
			if ( args[0] == null || args[1] == null ) {
				return true;
			}
			return !args[0].equals( args[1] );
		}
	}
}
