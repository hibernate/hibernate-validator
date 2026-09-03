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
import org.hibernate.validator.constraints.BIC.List;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/ISO_9362">BIC</a>
 * (Bank Identifier Code, also known as SWIFT code).
 * <p>
 * The constraint validates the structure per ISO 9362:
 * <ul>
 *     <li>Length must be exactly 8 or 11 characters</li>
 *     <li>Positions 1-4: Institution (bank) code - 4 letters</li>
 *     <li>Positions 5-6: Country code - 2 letters (ISO 3166-1 alpha-2, including 'XK' for Kosovo)</li>
 *     <li>Positions 7-8: Location code - 2 alphanumeric characters</li>
 *     <li>Positions 9-11: Branch code - 3 alphanumeric characters (optional)</li>
 * </ul>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * Optional filtering by country codes and bank codes is supported via the {@link #countryCodes()}
 * and {@link #bankCodes()} attributes.
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
public @interface BIC {

	String message() default "{org.hibernate.validator.constraints.BIC.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return whether to allow lowercase letters in the BIC.
	 * When {@code false} (default), only uppercase letters are accepted (per ISO 9362 standard).
	 * When {@code true}, lowercase letters are normalized to uppercase during validation.
	 */
	boolean allowLowercase() default false;

	/**
	 * @return the allowed ISO 3166-1 alpha-2 country codes (positions 5-6 of the BIC).
	 * An empty array (default) means all valid country codes are accepted.
	 * When specified, only BICs from the listed countries will be considered valid.
	 * Country codes are matched case-insensitively.
	 */
	String[] countryCodes() default { };

	/**
	 * @return the allowed 4-letter institution (bank) codes (positions 1-4 of the BIC).
	 * An empty array (default) means all bank codes are accepted.
	 * When specified, only BICs with the listed bank codes will be considered valid.
	 * Bank codes are matched case-insensitively.
	 */
	String[] bankCodes() default { };

	/**
	 * Defines several {@code @BIC} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		BIC[] value();
	}
}
