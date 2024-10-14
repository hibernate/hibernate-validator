/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Constraint(validatedBy = { AuthorBusinessRules.AuthorBusinessRulesValidator.class })
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorBusinessRules {
	String message() default "";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class AuthorBusinessRulesValidator implements ConstraintValidator<AuthorBusinessRules, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return true;
		}
	}
}
