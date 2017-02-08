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
import java.time.Period;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.time.PeriodMax.List;

/**
 * Annotated {@link Period} element's length must be less than or equal to
 * the one constructed from {@link PeriodMax#years()}, {@link PeriodMax#months()},
 * {@link PeriodMax#days()} values.
 *
 * @author Marko Bekhta
 * @hv.experimental
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
public @interface PeriodMax {

	String message() default "{org.hibernate.validator.constraints.time.PeriodMax.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	int years() default 0;

	int months() default 0;

	int days() default 0;

	/**
	 * @return a number of days present in a month. Is used to normalize number of days and months
	 * so that two {@link Period}s can be compared. By default a 30 day month is considered.
	 * Provided value should be {@code > 0}, otherwise an exception will be thrown.
	 */
	int daysInMonth() default 30;

	/**
	 * Defines several {@code @PeriodMax} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		PeriodMax[] value();
	}
}
