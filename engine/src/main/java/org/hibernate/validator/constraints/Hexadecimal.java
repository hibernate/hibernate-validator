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
import org.hibernate.validator.constraints.Hexadecimal.List;

/**
 * Checks that the annotated character sequence contains only hexadecimal characters
 * ({@code 0-9}, {@code a-f}, {@code A-F}).
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * Validation characteristics:
 * <ul>
 * <li>Letter case ({@code letterCase}): {@code LOWER_CASE} accepts only {@code 0-9a-f}, {@code UPPER_CASE} only
 * {@code 0-9A-F}, and {@code INSENSITIVE} (default) accepts any mix of cases.</li>
 * <li>Strictness ({@code strictness}): {@code STRICT} (default) accepts only the ASCII hex characters;
 * {@code LENIENT} additionally accepts the fullwidth forms ({@code ０-９}, {@code ａ-ｆ}, {@code Ａ-Ｆ}).</li>
 * <li>Empty strings: invalid by default, allowed if {@code allowEmpty} is {@code true}.</li>
 * <li>Prefix ({@code prefix}): an optional leading marker that must be present when set. Well-known prefixes
 * (see {@link HexPrefixes}) are matched exactly and case-sensitively; any other value is treated as a regular
 * expression which is anchored to the start of the value. The {@code letterCase} and {@code strictness}
 * attributes apply only to the characters following the prefix.</li>
 * </ul>
 *
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface Hexadecimal {

	String message() default "{org.hibernate.validator.constraints.Hexadecimal.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the required letter case for the hexadecimal letters.
	 * Per default, any letter case is accepted.
	 *
	 * @see LetterCase
	 */
	LetterCase letterCase() default LetterCase.INSENSITIVE;

	/**
	 * @return the accepted character set.
	 * Per default, only the ASCII hexadecimal characters are accepted.
	 *
	 * @see HexStrictness
	 */
	HexStrictness strictness() default HexStrictness.STRICT;

	/**
	 * @return whether an empty character sequence (i.e. one containing no hexadecimal characters) is valid.
	 * Per default, empty character sequences are not valid.
	 */
	boolean allowEmpty() default false;

	/**
	 * A leading marker which, when non-empty, must be present at the start of the value.
	 * <p>
	 * Well-known prefixes (see {@link HexPrefixes}) are matched exactly and case-sensitively.
	 * Any other non-empty value is interpreted as a regular expression and is anchored to the
	 * start of the value; it must not itself start with a {@code ^} character.
	 * <p>
	 * The {@code letterCase} and {@code strictness} attributes only apply to the characters
	 * following the prefix.
	 *
	 * @return the prefix to require (empty string means no prefix).
	 */
	String prefix() default "";

	/**
	 * Required letter case for the hexadecimal letters.
	 */
	enum LetterCase {

		/**
		 * Only lower case is valid
		 */
		LOWER_CASE,

		/**
		 * Only upper case is valid
		 */
		UPPER_CASE,

		/**
		 * Every letter case is valid
		 */
		INSENSITIVE
	}

	/**
	 * The accepted hexadecimal character set.
	 */
	enum HexStrictness {

		/**
		 * Only the ASCII hexadecimal characters ({@code 0-9}, {@code a-f}, {@code A-F}) are valid.
		 */
		STRICT,

		/**
		 * In addition to the ASCII hexadecimal characters, the fullwidth forms
		 * ({@code ０-９}, {@code ａ-ｆ}, {@code Ａ-Ｆ}) are valid as well.
		 */
		LENIENT
	}

	/**
	 * Well-known prefixes used with hexadecimal values in various contexts/languages.
	 */
	interface HexPrefixes {

		/**
		 * No prefix.
		 */
		String NONE = "";

		/**
		 * C/Java/Python-style hexadecimal literal prefix.
		 */
		String HEX_LITERAL = "0x";

		/**
		 * CSS hexadecimal color prefix.
		 */
		String CSS_COLOR = "#";
	}

	/**
	 * Defines several {@code @Hexadecimal} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Hexadecimal[] value();
	}
}
