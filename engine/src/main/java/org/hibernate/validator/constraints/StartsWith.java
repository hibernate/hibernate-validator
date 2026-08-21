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
import org.hibernate.validator.constraints.StartsWith.List;

/**
 * Validates that the annotated character sequence starts with the specified prefix(es).
 * <p>
 * When multiple values are specified, at least one must match (i.e., OR semantics).
 * <p>
 * When {@code ignoreCase} is set to {@code true}, the comparison is case-insensitive.
 * {@code null} values are considered valid.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface StartsWith {

	/**
	 * @return the prefixes of which at least one must match the start of the annotated character sequence.
	 */
	String[] value();

	/**
	 * @return whether to perform case-insensitive matching.
	 * When {@code false} (default), matching is case-sensitive.
	 * When {@code true}, both the input and the prefixes are compared
	 * using {@link java.util.Locale#ROOT} lowercasing.
	 */
	boolean ignoreCase() default false;

	String message() default "{org.hibernate.validator.constraints.StartsWith.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @StartsWith} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		StartsWith[] value();
	}
}
