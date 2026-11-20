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
import org.hibernate.validator.constraints.NormalizedWhitespace.List;

/**
 * Validates that whitespace in a {@code CharSequence} conforms to a set of {@link Rule rules}.
 * <p>
 * By default, only regular space characters ({@code ' '}) are accepted as whitespace and consecutive
 * whitespace characters are rejected.
 * <p>
 * {@code null} values and empty character sequences are considered valid.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Incubating
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface NormalizedWhitespace {

	String message() default "{org.hibernate.validator.constraints.NormalizedWhitespace.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the whitespace rules to enforce
	 */
	Rule[] value() default { Rule.ONLY_SPACE, Rule.NO_CONSECUTIVE };

	/**
	 * Rules for whitespace normalization.
	 */
	enum Rule {
		/**
		 * Rejects two or more consecutive whitespace characters.
		 */
		NO_CONSECUTIVE,
		/**
		 * Rejects tab characters ({@code \t}).
		 */
		NO_TABS,
		/**
		 * Rejects form feed ({@code \f}) and vertical tab ({@code \u000B}) characters.
		 */
		NO_FORM_FEED,
		/**
		 * Rejects any whitespace character other than regular space ({@code ' '}).
		 * This implies {@link #NO_TABS} and {@link #NO_FORM_FEED}.
		 */
		ONLY_SPACE
	}

	/**
	 * Defines several {@code @NormalizedWhitespace} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		NormalizedWhitespace[] value();
	}
}
