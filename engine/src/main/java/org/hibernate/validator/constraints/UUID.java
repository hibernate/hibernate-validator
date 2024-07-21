/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.validator.constraints.UUID.List;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier">UUID</a>.
 * <p>
 * Validation characteristics:
 * <ul>
 * <li>Consists only of numbers, hex characters and dashes</li>
 * <li>Has exact length of 36 characters</li>
 * <li>Number of hex digits in every group (8-4-4-4-12)</li>
 * <li>Nil UUID (default: allowed)</li>
 * <li>Allowed UUID versions (default: 1 to 5)</li>
 * <li>Allowed UUID variants (default: 0 to 2)</li>
 * <li>Letter case (default: lower case)</li>
 * </ul>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 *
 * @author Daniel Heid
 * @since 8.0.0
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface UUID {

	String message() default "{org.hibernate.validator.constraints.UUID.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return allow empty strings.
	 * Per default does not allow empty strings
	 */
	boolean allowEmpty() default false;

	/**
	 * @return {@code true} if nil UUIDs {@code 00000000-0000-0000-0000-000000000000} are valid
	 * Per default nil UUIDs are valid
	 */
	boolean allowNil() default true;

	/**
	 * Must not be lower than version 1
	 *
	 * @return the accepted UUID version numbers
	 * Per default versions 1 to 5 are allowed
	 */
	int[] version() default { 1, 2, 3, 4, 5 };

	/**
	 * Must not be lower than variant 0
	 *
	 * @return the allowed UUID variant numbers
	 * Per default variants 0 to 2 are allowed
	 */
	int[] variant() default { 0, 1, 2 };

	/**
	 * @return the required letter case
	 * Per default only lower case is valid
	 *
	 * @see LetterCase
	 */
	LetterCase letterCase() default LetterCase.LOWER_CASE;

	/**
	 * Required letter case for hex characters
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
	 * Defines several {@code @UUID} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		UUID[] value();
	}
}

