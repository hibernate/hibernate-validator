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
import org.hibernate.validator.constraints.Numeric.List;

/**
 * The annotated element must contain only digits, i.e. every character must satisfy
 * {@link Character#isDigit(char)}. Any letter, whitespace, sign, decimal separator or punctuation
 * character causes validation to fail.
 * <p>
 * Note that {@link Character#isDigit(char)} accepts any Unicode decimal digit of the Basic Multilingual
 * Plane, not only the ASCII digits {@code 0-9} (e.g. Arabic-Indic or fullwidth digits are considered valid),
 * and that digits from different scripts may be mixed. The check is performed on each {@code char}, so digits
 * outside the Basic Multilingual Plane (represented by surrogate pairs, e.g. {@code U+1D7CE}) are considered
 * invalid. Use {@code @Pattern(regexp = "[0-9]*")} to restrict values to ASCII digits.
 * <p>
 * {@code null} values and empty character sequences are considered valid.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface Numeric {

	String message() default "{org.hibernate.validator.constraints.Numeric.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @Numeric} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Numeric[] value();
	}
}
