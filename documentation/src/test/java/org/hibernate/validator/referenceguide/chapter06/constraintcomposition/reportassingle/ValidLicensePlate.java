/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.constraintcomposition.reportassingle;

//end::include[]
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

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
