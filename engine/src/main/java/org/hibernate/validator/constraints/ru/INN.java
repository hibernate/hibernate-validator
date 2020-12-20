/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints.ru;

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
import jakarta.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.ru.INN.List;

/**
 * Checks that the annotated character sequence is a valid russian taxpayer
 * identification number (INN in russian transliteration).
 *
 * @author Artem Boiarshinov
 * @see <a href="https://www.nalog.ru/rn77/fl/interest/inn/">russian taxpayer identification number</a>
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
public @interface INN {

	String message() default "{org.hibernate.validator.constraints.ru.INN.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Type type() default Type.ANY;

	/**
	 * Defines several {@code @INN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {

		INN[] value();
	}

	/**
	 * Defines the INN length. Valid lengths of INN are {@code 12} for individual usage
	 * and {@code 10} for juridical which are represented as {@link INN.Type#INDIVIDUAL}
	 * and {@link INN.Type#JURIDICAL} respectively.
	 * <p>
	 * Using {@link INN.Type#ANY} allows to validate values that could either be personal
	 * or juridical.
	 * In such case, INN type would be determined by the length of the corresponding value.
	 */
	enum Type {
		INDIVIDUAL,
		JURIDICAL,
		ANY
	}
}
