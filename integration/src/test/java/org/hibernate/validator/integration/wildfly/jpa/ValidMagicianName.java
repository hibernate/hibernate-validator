/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Gunnar Morling
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface ValidMagicianName {

	String message() default "{org.hibernate.validator.integration.wildfly.jpa.ValidMagicianName.message}";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default { };

	public static class ValidMagicianNameValidator implements ConstraintValidator<ValidMagicianName, String> {

		@Inject
		private ErrorNameProvider errorNameProvider;

		@Override
		public void initialize(ValidMagicianName constraintAnnotation) {
		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate( errorNameProvider.getErrorName() )
				.addConstraintViolation();

			return false;
		}
	}

	@ApplicationScoped
	public static class ErrorNameProvider {

		String getErrorName() {
			return "Invalid magician name";
		}
	}
}
