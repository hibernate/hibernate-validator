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
 * Validates that the annotated character sequence starts with at least one of the specified prefixes.
 * <p>
 * When {@code ignoreCase} is set to {@code true}, the comparison is case-insensitive using
 * {@link java.util.Locale#ROOT} lowercasing.
 * <p>
 * {@code null} values are considered valid.
 *
 * @author Andrea Boriero
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
	 * @return the prefixes, at least one of which the annotated character sequence must start with.
	 */
	String[] value();

	/**
	 * @return whether to perform case-insensitive matching.
	 * When {@code false} (default), matching is case-sensitive.
	 * When {@code true}, both the input and the prefix are compared
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
