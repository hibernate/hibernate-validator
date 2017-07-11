/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.specexample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
@NotNull
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = SecurityChecking.Validator.class)
@ReportAsSingleViolation
public @interface SecurityChecking {

	Class<?>[] groups() default { };

	String message() default "Default error message";

	Class<? extends Payload>[] payload() default { };

	class Validator implements ConstraintValidator<SecurityChecking, Author> {

		@Override
		public boolean isValid(Author value, ConstraintValidatorContext constraintValidatorContext) {
			return value.hasSecurityClearance();
		}
	}
}
