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
import java.time.temporal.ChronoUnit;
import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.time.DurationRange.List;

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
@DurationMin
@DurationMax
@ReportAsSingleViolation
public @interface DurationRange {

	String message() default "{org.hibernate.validator.constraints.time.DurationRange.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	@OverridesAttribute(constraint = DurationMin.class, name = "value") long min() default Long.MIN_VALUE;

	@OverridesAttribute(constraint = DurationMin.class, name = "units") ChronoUnit minUnits() default ChronoUnit.NANOS;

	@OverridesAttribute(constraint = DurationMax.class, name = "value") long max() default Long.MAX_VALUE;

	@OverridesAttribute(constraint = DurationMax.class, name = "units") ChronoUnit maxUnits() default ChronoUnit.NANOS;

	/**
	 * Defines several {@code @DurationMax} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		DurationRange[] value();
	}
}
