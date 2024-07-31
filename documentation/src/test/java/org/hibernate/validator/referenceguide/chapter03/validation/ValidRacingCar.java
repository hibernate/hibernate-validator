package org.hibernate.validator.referenceguide.chapter03.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidRacingCar.Validator.class })
@Documented
public @interface ValidRacingCar {

	String message() default "{org.hibernate.validator.referenceguide.chapter03.validation.ValidRacingCar.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class Validator implements ConstraintValidator<ValidRacingCar, Car> {

		@Override
		public void initialize(ValidRacingCar constraintAnnotation) {
		}

		@Override
		public boolean isValid(Car car, ConstraintValidatorContext context) {
			return false;
		}
	}
}
