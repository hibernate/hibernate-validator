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
import javax.validation.constraints.Size;

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
	@Constraint(validatedBy = { SomeValidator.class })
	public @interface CancellationCodeInvalid {

		String message() default "org.jboss.jdf.example.ticketmonster.model.CancellationCodeInvalid.message";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { SomeOtherValidator.class })
	public @interface CancellationCodeValid {

		String message() default "{org.jboss.jdf.example.ticketmonster.model.CancellationCodeInvalid.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
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

	public static class Case2 {

		@Size(message = "just some custom message which is completely fine")
		private String string1;

		@Size(message = "now.this.one.is.probably.not.what.user.wanted.to.get")
		private String string2;

		@Size(message = "{now.this.one.is.probably.not.what.user.wanted.to.get.as.well")
		private String string3;

		@Size(message = "{this.one.is.just.fine.even.though.probably.there.is.no.such.key}")
		private String string4;

		@Size.List({
				@Size(message = "now.this.one.is.probably.not.what.user.wanted.to.get"),
				@Size(message = "{now.this.one.is.probably.not.what.user.wanted.to.get.as.well"),
				@Size(message = "now.this.one.is.probably.not.what.user.wanted.to.get.as.well}"),
				@Size(message = "{this.one.is.just.fine.even.though.probably.there.is.no.such.key}")
		})
		private String string5;

	}

	public static class Case3 {

		@Target(ElementType.FIELD)
		@Retention(RetentionPolicy.RUNTIME)
		@Constraint(validatedBy = { SomeCustomValidator.class })
		@Size(message = "{user.wants.to.provide.a.custom.key.for.the.message.but.forgets.a.closing.bracket")
		public @interface SomeCustomConstraintAnnotation {

			String message() default "{some.valid.message.key}";

			Class<?>[] groups() default { };

			Class<? extends Payload>[] payload() default { };
		}

		private static class SomeCustomValidator implements ConstraintValidator<SomeCustomConstraintAnnotation, String> {

			@Override
			public void initialize(SomeCustomConstraintAnnotation constraintAnnotation) {

			}

			@Override
			public boolean isValid(String value, ConstraintValidatorContext context) {
				return false;
			}

		}
	}

	public static class Case4 {

		@Size(message = "Something is wrong. Really wrong.")
		private String strin1;

	}

}
