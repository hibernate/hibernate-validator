/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints.time;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.time.PeriodRange.List;

/**
 * The annotated element has to be in the appropriate range.
 *
 * @author Marko Bekhta
 * @hv.experimental
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@PeriodMin
@PeriodMax
@ReportAsSingleViolation
public @interface PeriodRange {

	String message() default "{org.hibernate.validator.constraints.time.PeriodRange.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	@OverridesAttribute(constraint = PeriodMin.class, name = "years") int minYears() default Integer.MIN_VALUE;

	@OverridesAttribute(constraint = PeriodMin.class, name = "months") int minMonths() default Integer.MIN_VALUE;

	@OverridesAttribute(constraint = PeriodMin.class, name = "days") int minDays() default Integer.MIN_VALUE;

	@OverridesAttribute(constraint = PeriodMax.class, name = "years") int maxYears() default Integer.MAX_VALUE;

	@OverridesAttribute(constraint = PeriodMax.class, name = "months") int maxMonths() default Integer.MAX_VALUE;

	@OverridesAttribute(constraint = PeriodMax.class, name = "days") int maxDays() default Integer.MAX_VALUE;

	@OverridesAttribute.List({
			@OverridesAttribute(constraint = PeriodMax.class, name = "daysInMonth"),
			@OverridesAttribute(constraint = PeriodMax.class, name = "daysInMonth")
	}) int daysInMonth() default 30;

	/**
	 * Defines several {@code @PeriodRange} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		PeriodRange[] value();
	}
}
