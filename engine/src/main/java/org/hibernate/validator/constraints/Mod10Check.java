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
 * <a href="http://en.wikipedia.org/wiki/Luhn_algorithm">@Modulo 10</a> check constraint.
 * <p>
 * Allows to validate that a series of digits pass the Mod10 checksum
 * algorithm. The classic Mod10 is calculated by summing up the digits, with every odd
 * digit (from right to left) value multiplied by a {@code multiplier}.
 * As example ISBN-13 is Modulo 10 checksum with {@code multiplier = 3}.
 * </p>
 * <p>
 * There are known cases of codes using multipliers for both even and odd
 * digits; To support this kind of implementations the Mod10 constraint uses the
 * {@code weight} option, which has the same effect as the multiplier but for even
 * numbers.
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
public @interface Mod10Check {
	String message() default "{org.hibernate.validator.constraints.Mod10Check.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The multiplier to be used for odd digits when calculating the Mod10 checksum.
	 */
	int multiplier() default 3;

	/**
	 * @return The weight to be used for even digits when calculating the Mod10 checksum.
	 */
	int weight() default 1;

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
	 * the following must hold true:<br>
	 * {@code checkDigitIndex > 0 && (checkDigitIndex < startIndex || checkDigitIndex >= endIndex}.
	 */
	int checkDigitIndex() default -1;

	/**
	 * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
	 * validation error ({@code false}).
	 */
	boolean ignoreNonDigitCharacters() default true;

	/**
	 * Defines several {@code @Mod10Check} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Mod10Check[] value();
	}
}
