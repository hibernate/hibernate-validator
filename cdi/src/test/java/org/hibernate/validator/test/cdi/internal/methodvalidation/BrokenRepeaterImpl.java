/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@Broken
@ValidateOnExecution
public class BrokenRepeaterImpl implements Repeater<String> {

	@BrokenConstraint
	BrokenRepeaterImpl() {
	}

	@Override
	public String repeat(String in) {
		return in;
	}

	@Override
	public String reverse(String in) {
		return null;
	}

	@Override
	public String getHelloWorld() {
		return null;
	}

	@Constraint(validatedBy = { BrokenConstraint.BrokenConstraintImpl.class })
	@Documented
	@Target({ ElementType.CONSTRUCTOR })
	@Retention(RUNTIME)
	public @interface BrokenConstraint {
		String message() default "foobar";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };


		class BrokenConstraintImpl implements ConstraintValidator<BrokenConstraint, Repeater> {

			@Override
			public void initialize(BrokenConstraint parameters) {
			}

			@Override
			public boolean isValid(Repeater repeater, ConstraintValidatorContext constraintValidatorContext) {
				return false;
			}
		}
	}
}
