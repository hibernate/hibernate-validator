package org.hibernate.validator.referenceguide.chapter02.containerelement.optional;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { MinTowingCapacity.MinTowingCapacityValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface MinTowingCapacity {
	long value();

	String message() default "{org.hibernate.validator.referenceguide.chapter02.containerelement.MinTowingCapacity.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class MinTowingCapacityValidator implements ConstraintValidator<MinTowingCapacity, Integer> {
		private long min;

		@Override
		public void initialize(MinTowingCapacity annotation) {
			min = annotation.value();
		}

		@Override
		public boolean isValid(Integer value, ConstraintValidatorContext context) {
			if ( value == null ) {
				return true;
			}

			return value >= min;
		}
	}
}
