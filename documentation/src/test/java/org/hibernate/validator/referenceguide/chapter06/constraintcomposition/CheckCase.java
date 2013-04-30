package org.hibernate.validator.referenceguide.chapter06.constraintcomposition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface CheckCase {

	String message() default "{org.hibernate.validator.referenceguide.chapter06.constraintcomposition.CheckCase.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	CaseMode value();
}
