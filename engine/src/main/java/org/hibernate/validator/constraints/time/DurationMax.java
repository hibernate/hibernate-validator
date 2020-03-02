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
import java.time.Duration;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.time.DurationMax.List;

/**
 * The annotated {@link Duration} element must be shorter than or equal to the one constructed as a sum of
 * {@link DurationMax#nanos()}, {@link DurationMax#millis()}, {@link DurationMax#seconds()},
 * {@link DurationMax#minutes()}, {@link DurationMax#hours()}, {@link DurationMax#days()}.
 *
 * @author Marko Bekhta
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
@Incubating
public @interface DurationMax {

	String message() default "{org.hibernate.validator.constraints.time.DurationMax.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	long days() default 0;

	long hours() default 0;

	long minutes() default 0;

	long seconds() default 0;

	long millis() default 0;

	long nanos() default 0;

	/**
	 * Specifies whether the specified maximum is inclusive or exclusive. By default, it is inclusive.
	 *
	 * @return {@code true} if the value must be smaller or equal to the specified maximum,
	 *         {@code false} if the value must be smaller
	 *
	 */
	boolean inclusive() default true;

	/**
	 * Defines several {@code @DurationMax} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		DurationMax[] value();
	}
}
