package org.hibernate.validator.referenceguide.chapter02.typeargument;

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
@Constraint(validatedBy = { MinTorque.MinTorqueValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface MinTorque {
	long value();

	String message() default "{org.hibernate.validator.referenceguide.chapter02.typeargument.MinTorque.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class MinTorqueValidator implements ConstraintValidator<MinTorque, Gear> {
		private long min;

		@Override
		public void initialize(MinTorque annotation) {
			this.min = annotation.value();
		}

		@Override
		public boolean isValid(Gear gear, ConstraintValidatorContext context) {
			if ( gear == null ) {
				return true;
			}

			return gear.getTorque() > min;
		}
	}
}
