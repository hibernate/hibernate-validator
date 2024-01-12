/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints.kor;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit.NEVER;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Checks that the annotated character sequence is a valid Korean resident registration number.
 *
 * @author Taewoo Kim
 * @see <a href="https://ko.wikipedia.org/wiki/%EC%A3%BC%EB%AF%BC%EB%93%B1%EB%A1%9D%EB%B2%88%ED%98%B8">Korean resident registration number</a>
 */

@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(KorRRN.List.class)
@ReportAsSingleViolation
public @interface KorRRN {

	String message() default "{org.hibernate.validator.constraints.kor.KorRRN.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	ValidateCheckDigit validateCheckDigit() default NEVER;

	/**
	 * Defines the validation rules this constraint applies to the Korean resident registration number.
	 * <p>
	 * Each type specifies which particular checks will be applied.
	 */
	enum ValidateCheckDigit {
		/**
		 * Perform the checks on:
		 * <ul>
		 *     <li>The length of an RRN</li>
		 *     <li>The validity of the Gender-Digit in an RRN</li>
		 *     <li>The validity of the date in an RRN</li>
		 * </ul>
		 */
		NEVER,
		/**
		 * Perform the checks on:
		 * <ul>
		 *     <li>The length of an RRN</li>
		 *     <li>The validity of the Gender-Digit in an RRN</li>
		 *     <li>The validity of the date in an RRN</li>
		 *     <li>The Validity of check-digit in RRN</li>
		 * </ul>
		 */
		ALWAYS
	}

	/**
	 * Defines several {@code @KorRRN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		KorRRN[] value();
	}
}
