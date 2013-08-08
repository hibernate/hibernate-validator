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
 * Modulo check constraint.
 * <p>
 * Allows to validate that a series of digits pass the mod 10 or mod 11 checksum algorithm.
 * </p>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * </p>
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @deprecated As of release 5.1.0, replaced by {@link Mod10Check} and {@link Mod11Check}
 */
@Documented
@Deprecated
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface ModCheck {
	String message() default "{org.hibernate.validator.constraints.ModCheck.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The modulus algorithm to be used
	 */
	ModType modType();

	/**
	 * @return The multiplier to be used by the chosen mod algorithm
	 */
	int multiplier();

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
	 * Defines several {@code @ModCheck} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		ModCheck[] value();
	}

	public enum ModType {
		/**
		 * Represents a MOD10 algorithm (Also known as Luhn algorithm)
		 */
		MOD10,
		/**
		 * Represents a MOD11 algorithm. A remainder of 10 or 11 in the algorithm is mapped to the check digit 0.
		 */
		MOD11
	}
}
