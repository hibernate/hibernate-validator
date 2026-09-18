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
import org.hibernate.validator.constraints.ISSN.List;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/International_Standard_Serial_Number">ISSN</a>.
 * The length of the number and the check digit are both verified.
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * During validation all non ISSN characters are ignored. All digits and 'X' are considered
 * to be valid ISSN characters. This is useful when validating ISSN with dashes separating
 * parts of the number (ex. {@code 1234-567X}).
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
public @interface ISSN {

	String message() default "{org.hibernate.validator.constraints.ISSN.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	Type type() default Type.ISSN_13;

	/**
	 * Defines several {@code @ISSN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {

		ISSN[] value();
	}

	/**
	 * Defines the ISSN length. Valid lengths of ISSNs are {@code 8} and {@code 13}
	 * which are represented as {@link Type#ISSN_8} and {@link Type#ISSN_13} respectively.
	 * <p>
	 * Using {@link Type#ANY} allows to validate values that could either be ISSN-8 or ISSN-13.
	 * In such case, ISSN type would be determined by the length of the corresponding value.
	 */
	enum Type {
		ISSN_8,
		ISSN_13,
		ANY
	}
}
