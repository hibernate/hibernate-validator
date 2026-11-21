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
import org.hibernate.validator.constraints.SingleLine.List;

/**
 * Validates that the annotated character sequence contains no line break characters,
 * i.e. it is a single line of text.
 * <p>
 * The following Unicode line break characters are rejected:
 * <ul>
 *     <li>{@code \n} (U+000A) — Line Feed</li>
 *     <li>{@code \r} (U+000D) — Carriage Return</li>
 *     <li>{@code \u0085} (U+0085) — Next Line (NEL)</li>
 *     <li>{@code \u2028} (U+2028) — Line Separator</li>
 *     <li>{@code \u2029} (U+2029) — Paragraph Separator</li>
 * </ul>
 * <p>
 * {@code null} and empty sequences are considered valid.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface SingleLine {

	String message() default "{org.hibernate.validator.constraints.SingleLine.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @SingleLine} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		SingleLine[] value();
	}
}
