/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;


import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be an instant, date or time for which at least
 * the specified amount ({@link AgeMin#value()}) of Years/Days/Months/etc. defined
 * by {@link AgeMin#unit()} have passed till now.
 * <p>
 * Supported types are:
 * <ul>
 *     <li>{@code java.util.Calendar}</li>
 *     <li>{@code java.util.Date}</li>
 *     <li>{@code java.time.chrono.HijrahDate}</li>
 *     <li>{@code java.time.chrono.JapaneseDate}</li>
 *     <li>{@code java.time.LocalDate}</li>
 *     <li>{@code java.time.chrono.MinguoDate}</li>
 *     <li>{@code java.time.chrono.ThaiBuddhistDate}</li>
 *     <li>{@code java.time.Year}</li>
 *     <li>{@code java.time.YearMonth}</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 *
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(AgeMin.List.class)
@Documented
@Constraint(validatedBy = {})
public @interface AgeMin {

	String message() default "{org.hibernate.validator.constraints.AgeMin.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return the age according to unit from a given instant, date or time must be greater or equal to
	 */
	int value();

	/**
	 * Specifies the date period unit ( Years/Days/Months/etc. ) that will be used to compare the given instant,
	 * date or time with the reference value.
	 * By default, it is ({@link ChronoUnit#YEARS}).
	 *
	 * @return the date period unit
	 */
	ChronoUnit unit() default ChronoUnit.YEARS;

	/**
	 * Specifies whether the specified value is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the date period units from a given instant, date or time must be higher or equal to the specified value,
	 *         {@code false} if date period units from a given instant, date or time must be higher
	 */
	boolean inclusive() default true;

	/**
	 * Defines several {@link AgeMin} annotations on the same element.
	 *
	 * @see AgeMin
	 */
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@Retention(RUNTIME)
	@Documented
	@interface List {
		AgeMin[] value();
	}


}
