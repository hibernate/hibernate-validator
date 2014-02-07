/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Modulo 11 check constraint.
 * <p>
 * Allows to validate that a series of digits passes the Mod11 checksum
 * algorithm.
 * For the most common Mod11 variant the sum calculation is done by multiplying a weight from
 * the rightmost digit (excluding the check digit) to the leftmost. The weight
 * starts with 2 and increases by 1 for each digit. Then the result is used to
 * calculate the check digit using {@code 11 - ( sum % 11 )}.
 * </p>
 * <p>
 * Example: The check digit for 24187 is 3<br />
 * Sum = 7x2 + 8x3 + 1x4 + 4x5 + 2x6 = 74 <br />
 * 11 - (74 % 11) = 11 - 8 = 3, so "24187-3" is a valid character sequence.
 * </p>
 * <p>
 * The Mod11 calculation can result in 10 or 11; per default 10 is treated as
 * {@code 'X'} and 11 as {@code '0'}, this behavior can be changed using the
 * options {@code treatCheck10As} and {@code treatCheck10As}.
 * </p>
 * <p>
 * Some implementations do the sum calculation in the reverse order (left to right);
 * specify the processing direction {@link ProcessingDirection#LEFT_TO_RIGHT} in
 * this case.
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
public @interface Mod11Check {
	String message() default "{org.hibernate.validator.constraints.Mod11Check.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The threshold for the Mod11 algorithm multiplier growth, if no value is specified the multiplier will grow indefinitely
	 */
	int threshold() default Integer.MAX_VALUE;

	/**
	 * @return the start index (inclusive) for calculating the checksum. If not specified 0 is assumed.
	 */
	int startIndex() default 0;

	/**
	 * @return the end index (inclusive) for calculating the checksum. If not specified the whole value is considered
	 */
	int endIndex() default Integer.MAX_VALUE;

	/**
	 * @return The index of the check digit in the input. Per default it is assumed that the check digit is the last
	 * digit of the specified range. If set, the digit at the specified index is used. If set
	 * the following must hold true:<br/>
	 * {@code checkDigitIndex > 0 && (checkDigitIndex < startIndex || checkDigitIndex >= endIndex}.
	 */
	int checkDigitIndex() default -1;

	/**
	 * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
	 * validation error ({@code false}).
	 */
	boolean ignoreNonDigitCharacters() default false;

	/**
	 * @return The {@code char} that represents the check digit when the Mod11
	 * checksum equals 10. If not specified {@code 'X'} is assumed.
	 */
	char treatCheck10As() default 'X';

	/**
	 * @return The {@code char} that represents the check digit when the Mod11
	 * checksum equals 11. If not specified {@code '0'} is assumed.
	 */
	char treatCheck11As() default '0';

	/**
	 * @return Returns {@code RIGHT_TO_LEFT} if the Mod11 checksum must be done from the rightmost to the leftmost digit.
	 * e.g. Code 12345-?:
	 * <ul>
	 * <li>{@code RIGHT_TO_LEFT} the sum (5*2 + 4*3 + 3*4 + 2*5 + 1*6) with check digit 5</li>
	 * <li>{@code LEFT_TO_RIGHT} the sum (1*2 + 2*3 + 3*4 + 4*5 + 5*6) with check digit 7</li>
	 * </ul>
	 * If not specified {@code RIGHT_TO_LEFT} is assumed, it is the default Mod11 behavior.
	 */
	ProcessingDirection processingDirection() default ProcessingDirection.RIGHT_TO_LEFT;

	/**
	 * Defines several {@code @Mod11Check} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Mod11Check[] value();
	}

	public enum ProcessingDirection {
		RIGHT_TO_LEFT,
		LEFT_TO_RIGHT
	}
}
