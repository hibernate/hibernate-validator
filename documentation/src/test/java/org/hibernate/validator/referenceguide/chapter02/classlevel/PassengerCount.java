package org.hibernate.validator.referenceguide.chapter02.classlevel;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { PassengerCount.Validator.class })
@Documented
public @interface PassengerCount {

	String message() default "{org.hibernate.validator.referenceguide.chapter02.classlevel.PassengerCount.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	public static class Validator
			implements ConstraintValidator<PassengerCount, Car> {

		@Override
		public void initialize(PassengerCount constraintAnnotation) {
		}

		@Override
		public boolean isValid(Car car, ConstraintValidatorContext context) {
			return false;
		}
	}
}
