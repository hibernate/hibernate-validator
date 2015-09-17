/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.executable.ValidateOnExecution;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

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


		public class BrokenConstraintImpl implements ConstraintValidator<BrokenConstraint, Repeater> {

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
