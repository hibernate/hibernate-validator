//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.classlevel;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//end::include[]
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

//tag::include[]
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidPassengerCountValidator.class })
@Documented
public @interface ValidPassengerCount {

	String message() default "{org.hibernate.validator.referenceguide.chapter06.classlevel." +
			"ValidPassengerCount.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
//end::include[]
