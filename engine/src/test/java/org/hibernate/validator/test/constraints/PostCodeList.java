/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * A test constraint which can lead to a error when trying to reslove the validator.
 *
 * @author Hardy Ferentschik
 */
@Constraint(validatedBy = {
		PostCodeList.PostCodeListValidatorForString.class,
		PostCodeList.PostCodeListValidatorForNumber.class
})
@Documented
@Target({ METHOD, FIELD, TYPE })
@Retention(RUNTIME)
public @interface PostCodeList {
	String message() default "foobar";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class PostCodeListValidatorForNumber
			implements ConstraintValidator<PostCodeList, Collection<? extends Number>> {

		@Override
		public boolean isValid(Collection<? extends Number> value, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}

	class PostCodeListValidatorForString implements ConstraintValidator<PostCodeList, Collection<String>> {

		@Override
		public boolean isValid(Collection<String> value, ConstraintValidatorContext constraintValidatorContext) {
			if ( value == null ) {
				return true;
			}
			return false;
		}
	}
}
