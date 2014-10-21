/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Luhn algorithm check constraint.
 * <p>
 * Allows to validate that a series of digits pass the Luhn Modulo 10 checksum
 * algorithm. The Luhn Mod10 is calculated by summing up the digits, with every odd
 * digit (from right to left) value multiplied by 2, if the value is greater than 9 the
 * the result digits a summed before the total summing.
 * </p>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * </p>
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface LuhnCheck {
	String message() default "{org.hibernate.validator.constraints.LuhnCheck.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the start index (inclusive) for calculating the checksum. If not specified 0 is assumed.
	 */
	int startIndex() default 0;

	/**
	 * @return the end index (inclusive) for calculating the checksum. If not specified the whole value is considered.
	 */
	int endIndex() default Integer.MAX_VALUE;

	/**
	 * @return The index of the check digit in the input. Per default it is assumed that the check digit is the last
	 * digit of the specified range. If set, the digit at the specified index is used. If set
	 * the following must hold true:
	 * {@code checkDigitIndex > 0 && (checkDigitIndex < startIndex || checkDigitIndex >= endIndex}.
	 */
	int checkDigitIndex() default -1;

	/**
	 * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
	 * validation error ({@code false}).
	 */
	boolean ignoreNonDigitCharacters() default true;

	/**
	 * Defines several {@code @LuhnCheck} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		LuhnCheck[] value();
	}
}
