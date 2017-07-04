//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintcomposition.reportassingle;

//end::include[]

import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

//tag::include[]
//...
@ReportAsSingleViolation
public @interface ValidLicensePlate {

	String message() default "{org.hibernate.validator.referenceguide.chapter06." +
			"constraintcomposition.reportassingle.ValidLicensePlate.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
//end::include[]
