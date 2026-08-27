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
import org.hibernate.validator.constraints.IBAN.List;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/International_Bank_Account_Number">IBAN</a>
 * (International Bank Account Number).
 * <p>
 * The country-specific length and the ISO 7064 MOD 97-10 check digits are both verified.
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * During validation spaces are ignored. This is useful when validating IBANs that use spaces
 * to separate groups of characters (ex. {@code GB82 WEST 1234 5698 7654 32}).
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
public @interface IBAN {

	String message() default "{org.hibernate.validator.constraints.IBAN.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	boolean allowLowercase() default false;

	/**
	 * Defines several {@code @IBAN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		IBAN[] value();
	}
}
