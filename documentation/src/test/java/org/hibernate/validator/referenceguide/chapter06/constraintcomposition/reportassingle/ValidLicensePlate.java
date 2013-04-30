package org.hibernate.validator.referenceguide.chapter06.constraintcomposition.reportassingle;

import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

//...
@ReportAsSingleViolation
public @interface ValidLicensePlate {

	String message() default "{org.hibernate.validator.referenceguide.chapter06." +
			"constraintcomposition.ValidLicensePlate.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
