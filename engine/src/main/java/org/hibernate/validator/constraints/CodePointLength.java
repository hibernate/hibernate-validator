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
import java.text.Normalizer;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.CodePointLength.List;


/**
 * Validate that the code point length of a character sequence is between min and max included.
 * <p>
 * It is possible to validate a normalized value by setting the normalization strategy.
 *
 * @author Kazuki Shimizu
 * @version 6.0.3
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface CodePointLength {

	int min() default 0;

	int max() default Integer.MAX_VALUE;

	NormalizationStrategy normalizationStrategy() default NormalizationStrategy.NONE;

	String message() default "{org.hibernate.validator.constraints.CodePointLength.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @CodePointLength} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		CodePointLength[] value();
	}

	/**
	 * Strategy for normalization.
	 *
	 * @see Normalizer
	 * @see Normalizer.Form
	 */
	enum NormalizationStrategy {

		/**
		 * No normalization.
		 */
		NONE(null),

		/**
		 * Normalization by canonical decomposition.
		 */
		NFD(Normalizer.Form.NFD),

		/**
		 * Normalization by canonical decomposition, followed by canonical composition.
		 */
		NFC(Normalizer.Form.NFC),

		/**
		 * Normalization by compatibility decomposition.
		 */
		NFKD(Normalizer.Form.NFKD),

		/**
		 * Normalization by compatibility decomposition, followed by canonical composition.
		 */
		NFKC(Normalizer.Form.NFKC);

		private final Normalizer.Form form;

		NormalizationStrategy(Normalizer.Form form) {
			this.form = form;
		}

		/**
		 * Normalize a specified character sequence.
		 * @param value target value
		 * @return normalized value
		 */
		public CharSequence normalize(CharSequence value) {
			if ( this.form == null || value == null || value.length() == 0 ) {
				return value;
			}
			return Normalizer.normalize( value, this.form );
		}
	}
}

