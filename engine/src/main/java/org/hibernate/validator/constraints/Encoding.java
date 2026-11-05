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
import org.hibernate.validator.constraints.Encoding.List;

/**
 * Checks that every character of the annotated character sequence can be encoded in the given
 * {@link java.nio.charset.Charset charset} (e.g. {@code US-ASCII} or {@code ISO-8859-1}).
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * Validation characteristics:
 * <ul>
 * <li>Charset ({@code charset}): any charset name or alias supported by the running JVM. An unknown charset,
 * an illegal charset name or a charset that does not support encoding causes an exception when the
 * constraint is initialized.</li>
 * <li>Empty character sequences are considered valid.</li>
 * <li>Unpaired surrogates are never encodable and are therefore considered invalid. Characters outside the
 * Basic Multilingual Plane (e.g. most emoji) are represented by a pair of {@code char} values, a high surrogate
 * ({@code U+D800}-{@code U+DBFF}) followed by a low surrogate ({@code U+DC00}-{@code U+DFFF}); a surrogate
 * without its counterpart (e.g. in a string truncated in the middle of an emoji) does not represent any
 * character.</li>
 * <li>No Unicode normalization is applied: the character sequence is checked as is. Combine with
 * {@link Normalized} if the value is expected to be normalized first.</li>
 * </ul>
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface Encoding {

	String message() default "{org.hibernate.validator.constraints.Encoding.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the name (or alias) of the charset in which every character must be encodable,
	 * e.g. {@code US-ASCII} or {@code ISO-8859-1}.
	 *
	 * @see java.nio.charset.Charset#forName(String)
	 */
	String charset();

	/**
	 * Defines several {@code @Encoding} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Encoding[] value();
	}
}
