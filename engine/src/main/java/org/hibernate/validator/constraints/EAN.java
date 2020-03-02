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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.EAN.List;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="http://en.wikipedia.org/wiki/International_Article_Number_%28EAN%29">EAN 13</a> number. The length of the
 * number and the check digit are verified
 *
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * </p>
 *
 * @author Hardy Ferentschik
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
@Mod10Check
public @interface EAN {
	String message() default "{org.hibernate.validator.constraints.EAN.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	Type type() default Type.EAN13;

	/**
	 * Defines several {@code @EAN} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		EAN[] value();
	}

	enum Type {
		EAN13,
		EAN8
	}
}
