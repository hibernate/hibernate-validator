package org.hibernate.validator.referenceguide.chapter07;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidRentalStation.Validator.class })
@Documented
public @interface ValidRentalStation {

	String message() default "{org.hibernate.validator.referenceguide.chapter03.returnvalue.ValidRentalStation.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	public static class Validator
			implements ConstraintValidator<ValidRentalStation, Object[]> {

		@Override
		public void initialize(ValidRentalStation constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object[] value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
