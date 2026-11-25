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
import org.hibernate.validator.constraints.Finite.List;

/**
 * Validates that the annotated floating-point number is finite, i.e. it is
 * neither {@link Double#NaN NaN} nor {@linkplain Double#isInfinite() infinite}.
 * <p>
 * Supported types are {@code float}, {@code double}, {@link Float} and {@link Double}.
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
public @interface Finite {

	String message() default "{org.hibernate.validator.constraints.Finite.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @Finite} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Finite[] value();
	}
}
