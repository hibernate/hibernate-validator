/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

import org.hibernate.validator.constraints.ISBN.List;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/International_Standard_Book_Number">ISBN</a>.
 * The length of the number and the check digit are both verified.
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * <p>
 * During validation all non ISBN characters are ignored. All digits and 'X' are considered
 * to be valid ISBN characters. This is useful when validating ISBN with dashes separating
 * parts of the number (ex. {@code 978-161-729-045-9}).
 *
 * @author Marko Bekhta
 * @since 6.0.6
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface ISBN {

	String message() default "{org.hibernate.validator.constraints.ISBN.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	Type type() default Type.ISBN_13;

	/**
	 * Defines several {@code @ISBN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {

		ISBN[] value();
	}

	/**
	 * Defines the ISBN length. Valid lengths of ISBNs are {@code 10} and {@code 13}
	 * which are represented as {@link Type#ISBN_10} and {@link Type#ISBN_13} respectively.
	 * <p>
	 * Using {@link Type#ANY} allows to validate values that could either be ISBN10 or ISBN13.
	 * In such case, ISBN type would be determined by the length of the corresponding value.
	 */
	enum Type {
		ISBN_10,
		ISBN_13,
		ANY
	}
}
