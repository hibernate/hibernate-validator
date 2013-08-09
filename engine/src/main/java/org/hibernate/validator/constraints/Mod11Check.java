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
 * Allows to validate that a series of digits pass the mod 11 checksum
 * algorithm. <br />
 * The most common Mod11 variant the sum is done by multiplying a weight from
 * the rightmost digit (excluding the check digit) to the leftmost, the weight 2
 * starts with 2 an increases by one each new digit, then the result is used to
 * calculate the check digit using {@code 11 - ( sum % 11 )}
 * </p>
 * <p>
 * Example: The check digit for 24187 is 3<br />
 * Sum = 7x2 + 8x3 + 1x4 + 4x5 + 2x6 = 74 <br />
 * 11 - (74 % 11) = 11 - 8 = 3 so 24187-3 is valid number.
 * </p>
 * <p>
 * Mod11 check digit can result in 10 or 11, per default 10 is treat as
 * {@code 'X'} and 11 as {@code '0'}, this behavior can be changed using the
 * options {@code treatCheck10As} and {@code treatCheck10As} No special
 * characters are accepted the digit must be a Letter or a Digit
 *
 * Some implementations do the sum in the reverse order (left to right),
 * </p>
 *
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
	 * @return The multiplier to be used by odd digits on Mod10 algorithm
	 */
	int multiplier() default 11;

	/**
	 * @return the start index (inclusive) for calculating the checksum. If not specified 0 is assumed.
	 */
	int startIndex() default 0;

	/**
	 * @return the end index (exclusive) for calculating the checksum. If not specified the whole value is considered
	 */
	int endIndex() default Integer.MAX_VALUE;

	/**
	 * @return The position of the check digit in input. Per default it is assumes that the check digit is part of the
	 *         specified range. If set, the digit at the specified position is used as check digit. If set it the following holds
	 *         true: {@code checkDigitPosition > 0 && (checkDigitPosition < startIndex || checkDigitPosition >= endIndex}.
	 */
	int checkDigitPosition() default -1;

	/**
	 * @return Returns {@code true} if non digit characters should be ignored, {@code false} if a non digit character
	 *         results in a validation error. {@code startIndex} and {@code endIndex} are always only referring to digit
	 *         characters.
	 */
	boolean ignoreNonDigitCharacters() default true;

	/**
	 * @return The {@code char} that represents the check digit when mod11
	 *         checksum equals 10. If not specified {@code '0'} is assumed.
	 */
	char treatCheck10As() default 'X';

	/**
	 * @return The {@code char} that represents the check digit when mod11
	 *         checksum equals 10. If not specified {@code 'X'} is assumed.
	 */
	char treatCheck11As() default '0';

	/**
	 * @return Returns {@code RIGHT_TO_LEFT} if the Mod11 checksum must be done from the rightmost to the leftmost digit.
	 *         e.g. Code 12345-?
	 *         {@code RIGHT_TO_LEFT} the sum (5*2 + 4*3 + 3*4 + 2*5 + 1*6) with check digit 5
	 *         {@code LEFT_TO_RIGHT} the sum (1*2 + 2*3 + 3*4 + 4*5 + 5*6) with check digit 7
	 *         If not specified {@code RIGHT_TO_LEFT} is assumed, it is the default Mod11 behavior.
	 */
	ProcessingDirection reverseOrder() default ProcessingDirection.RIGHT_TO_LEFT;

	/**
	 * Defines several {@code @ModCheck11} annotations on the same element.
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
