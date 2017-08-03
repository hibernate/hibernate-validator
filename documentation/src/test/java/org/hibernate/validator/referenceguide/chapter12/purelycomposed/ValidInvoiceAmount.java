// tag::include[]
package org.hibernate.validator.referenceguide.chapter12.purelycomposed;

//end::include[]
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//tag::include[]
@Min(value = 0)
@NotNull
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@ReportAsSingleViolation
public @interface ValidInvoiceAmount {

	String message() default "{org.hibernate.validator.referenceguide.chapter11.purelycomposed."
			+ "ValidInvoiceAmount.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@OverridesAttribute(constraint = Min.class, name = "value")
	long value();
}
//end::include[]
