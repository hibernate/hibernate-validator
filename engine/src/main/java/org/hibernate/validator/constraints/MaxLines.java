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
import org.hibernate.validator.constraints.MaxLines.List;

/**
 * Validates that the annotated character sequence has at most the specified number of lines.
 * <p>
 * A line is delimited by any of the following Unicode line break characters:
 * {@code \n} (LF), {@code \r} (CR), {@code \u0085} (NEL), {@code \u2028} (Line Separator)
 * and {@code \u2029} (Paragraph Separator). The {@code \r\n} (CRLF) sequence counts as a
 * single line break.
 * <p>
 * A string with no line breaks is considered to have one line.
 * <p>
 * {@code null} is considered valid.
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
public @interface MaxLines {

	int value();

	String message() default "{org.hibernate.validator.constraints.MaxLines.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @MaxLines} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		MaxLines[] value();
	}
}
