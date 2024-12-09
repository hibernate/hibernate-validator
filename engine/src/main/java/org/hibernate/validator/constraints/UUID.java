/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.validator.constraints.UUID.List;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier">UUID</a>.
 * <p>
 * Validation characteristics:
 * <ul>
 * <li>Consists only of numbers, hex characters and dashes</li>
 * <li>Has exact length of 36 characters</li>
 * <li>Number of hex digits in every group (8-4-4-4-12)</li>
 * <li>Nil UUID (default: allowed)</li>
 * <li>Allowed UUID versions (default: 1 to 5)</li>
 * <li>Allowed UUID variants (default: 0 to 2)</li>
 * <li>Letter case (default: lower case)</li>
 * </ul>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 *
 * @author Daniel Heid
 * @since 8.0.0
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface UUID {

	String message() default "{org.hibernate.validator.constraints.UUID.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return allow empty strings.
	 * Per default does not allow empty strings.
	 */
	boolean allowEmpty() default false;

	/**
	 * @return {@code true} if nil UUIDs {@code 00000000-0000-0000-0000-000000000000} are valid.
	 * Per default nil UUIDs are valid.
	 */
	boolean allowNil() default true;

	/**
	 * Accepts values in the {@code [1; 15]} range, which corresponds to the hexadecimal {@code [1; f]} range.
	 *
	 * @return the accepted UUID version numbers.
	 * Per default, versions 1 through 5 are allowed.
	 */
	int[] version() default { 1, 2, 3, 4, 5 };

	/**
	 * Accepts values in the {@code [0; 2]} range.
	 * <p>
	 * The variant of the UUID is determined by the binary representation of the 17th hex digit
	 * ({@code xxxxxxxx-xxxx-xxxx-Vxxx-xxxxxxxxxxxx} where {@code V} is the variant digit).
	 * <p>
	 * Currently, only variants {@code [0, 1, 2]} are supported by the validator:
	 * <table>
	 *     <caption>Table 1</caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Variant #</th>
	 *             <th>Binary Representation</th>
	 *             <th>Hex Digit</th>
	 *             <th>Comment</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>0</td>
	 *             <td>0xxx</td>
	 *             <td>0 - 7</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>1</td>
	 *             <td>10xx</td>
	 *             <td>8 - b</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>2</td>
	 *             <td>110x</td>
	 *             <td>c - d</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>-</td>
	 *             <td>1110</td>
	 *             <td>e</td>
	 *             <td>Unsupported, an UUID with such variant will be considered invalid.</td>
	 *         </tr>
	 *         <tr>
	 *             <td>-</td>
	 *             <td>1111</td>
	 *             <td>f</td>
	 *             <td>Unsupported, an UUID with such variant will be considered invalid.</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 *
	 * @return the allowed UUID variant numbers
	 * Per default, all variants 0 to 2 are allowed
	 */
	int[] variant() default { 0, 1, 2};

	/**
	 * @return the required letter case
	 * Per default only lower case is valid
	 *
	 * @see LetterCase
	 */
	LetterCase letterCase() default LetterCase.LOWER_CASE;

	/**
	 * Required letter case for hex characters
	 */
	enum LetterCase {

		/**
		 * Only lower case is valid
		 */
		LOWER_CASE,

		/**
		 * Only upper case is valid
		 */
		UPPER_CASE,

		/**
		 * Every letter case is valid
		 */
		INSENSITIVE

	}

	/**
	 * Defines several {@code @UUID} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		UUID[] value();
	}
}

