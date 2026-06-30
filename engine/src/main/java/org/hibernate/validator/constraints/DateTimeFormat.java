/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints;

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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.Incubating;

/**
 * Validates that the annotated character sequence represents a valid date and/or time
 * according to the specified {@link #pattern() format}.
 * <p>
 * The format pattern follows the same syntax as {@link java.time.format.DateTimeFormatter}.
 * For example, {@code "yyyy-MM-dd"} for dates like {@code "2026-07-06"},
 * or {@code "yyyy-MM-dd'T'HH:mm:ss"} for date-times like {@code "2026-07-06T11:00:00"}.
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 *
 * @author Sean Okafor
 * @since 9.2
 *
 * @see java.time.format.DateTimeFormatter
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(DateTimeFormat.List.class)
@Incubating
public @interface DateTimeFormat {

	String message() default "{org.hibernate.validator.constraints.DateTimeFormat.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Must be a valid pattern for {@link java.time.format.DateTimeFormatter}.
	 */
	String pattern();

	/**
	 * @return the locale(s) to use for parsing text-based date/time elements (e.g., month names, day names).
	 * When multiple locales are specified, validation succeeds if parsing succeeds with any of them (OR logic).
	 * Defaults to {@code "ROOT"}, which works for numeric patterns but not for text-based patterns
	 * like {@code "dd MMMM yyyy"}.
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>{@code locale = "en-US"} - Accept only English month/day names</li>
	 *   <li>{@code locale = {"en-US", "fr-FR", "de-DE"}} - Accept English, French, or German</li>
	 * </ul>
	 */
	String[] locale() default { "ROOT" };

	/**
	 * @return whether to be lenient when parsing dates.
	 * When false (default), invalid dates like February 31st are rejected
	 * using {@link java.time.format.ResolverStyle#STRICT}.
	 * When true, invalid dates are accepted and rolled over to subsequent months
	 * (e.g., February 30th becomes March 2nd) using {@link java.time.format.ResolverStyle#LENIENT}.
	 */
	boolean lenient() default false;

	/**
	 * Defines several {@code @DateTimeFormat} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		DateTimeFormat[] value();
	}

}
