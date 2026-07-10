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
import org.hibernate.validator.constraints.Contains.List;

/**
 * Validates that the annotated character sequence contains all (or a minimum number of) the specified substrings.
 * <p>
 * By default, all specified values must be present (AND semantics). Use {@code minRequired} to require only
 * a minimum number of matches (e.g., {@code minRequired=1} for OR semantics).
 * <p>
 * When {@code ignoreCase} is set to {@code true}, the comparison is case-insensitive.
 * {@code null} values are considered valid.
 *
 * @author Sean Okafor
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface Contains {
	/**
	 * Sentinel value indicating that all provided substrings must be present.
	 */
	int MATCH_ALL = -1;

	/**
	 * @return the substrings that must be present in the annotated character sequence.
	 */
	String[] value();

	/**
	 * @return the minimum number of substrings that must be present for validation to pass.
	 * Defaults to {@link #MATCH_ALL}, meaning all substrings must be present (AND semantics).
	 * Set to {@code 1} for OR semantics, or any other positive value for a custom threshold.
	 */
	int minRequired() default MATCH_ALL;

	/**
	 * @return whether to perform case-insensitive matching.
	 * When {@code false} (default), matching is case-sensitive.
	 * When {@code true}, both the input and the substrings are compared
	 * using {@link java.util.Locale#ROOT} lowercasing.
	 */
	boolean ignoreCase() default false;

	String message() default "{org.hibernate.validator.constraints.Contains.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @Contains} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Contains[] value();
	}
}
