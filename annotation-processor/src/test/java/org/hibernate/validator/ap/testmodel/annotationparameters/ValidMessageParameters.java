/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * @author Marko Bekhta
 */
public class ValidMessageParameters {

	private static class SomeValidator implements ConstraintValidator<CancellationCodeInvalid, String> {

		@Override
		public void initialize(CancellationCodeInvalid constraintAnnotation) {

		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}

	}

	private static class SomeOtherValidator implements ConstraintValidator<CancellationCodeValid, String> {

		@Override
		public void initialize(CancellationCodeValid constraintAnnotation) {

		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}

	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = {SomeValidator.class})
	public @interface CancellationCodeInvalid {

		String message() default "org.jboss.jdf.example.ticketmonster.model.CancellationCodeInvalid.message";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = {SomeOtherValidator.class})
	public @interface CancellationCodeValid {

		String message() default "{org.jboss.jdf.example.ticketmonster.model.CancellationCodeInvalid.message}";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}


	public static class Case1 {

		@CancellationCodeInvalid
		private String string;

		@CancellationCodeInvalid(message = "some overridden message")
		private String string1;

		@CancellationCodeValid
		private String string2;

		@CancellationCodeValid(message = "some.bad.overridden.message.example")
		private String string3;

	}

}
