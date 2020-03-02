package org.hibernate.validator.referenceguide.chapter10;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ValidCar {

	String message() default "{org.hibernate.validator.referenceguide.chapter08.ValidCar.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
