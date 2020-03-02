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

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

/**
 * @author Gunnar Morling
 */
@Target(METHOD)
@Retention(RUNTIME)
@Constraint(validatedBy = NewPasswordsIdentical.Validator.class)
@ReportAsSingleViolation
public @interface NewPasswordsIdentical {

	Class<?>[] groups() default { };

	String message() default "Default error message";

	Class<? extends Payload>[] payload() default { };

	@SupportedValidationTarget(ValidationTarget.PARAMETERS)
	class Validator implements ConstraintValidator<NewPasswordsIdentical, Object[]> {

		@Override
		public boolean isValid(Object[] args, ConstraintValidatorContext constraintValidatorContext) {
			if ( args[1] == null || args[2] == null ) {
				return true;
			}

			if ( !args[1].equals( args[2] ) ) {
				constraintValidatorContext.disableDefaultConstraintViolation();
				constraintValidatorContext
						.buildConstraintViolationWithTemplate( "New passwords are not identical" )
						.addParameterNode( 2 )
						.addConstraintViolation();

				return false;
			}
			else {
				return true;
			}
		}
	}
}
