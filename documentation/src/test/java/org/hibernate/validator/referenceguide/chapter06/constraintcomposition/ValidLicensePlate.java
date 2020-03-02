//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintcomposition;

//end::include[]

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//tag::include[]
@NotNull
@Size(min = 2, max = 14)
@CheckCase(CaseMode.UPPER)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@Documented
public @interface ValidLicensePlate {

	String message() default "{org.hibernate.validator.referenceguide.chapter06." +
			"constraintcomposition.ValidLicensePlate.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
//end::include[]
