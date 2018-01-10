/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a date where the number of years go by to today must be
 * greater or equal to the specified value
 * <p>
 * <p>
 * The supported type is {@code LocalDate}. {@code null} is considered valid.
 * <p>
 *
 * @author Hillmer Chona
 * @since 6.0.x
 */

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(MinAge.List.class)
@Documented
@Constraint(validatedBy = {})
public @interface MinAge {

	String message() default "{org.hibernate.validator.constraints.MinAge.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return value the age must be greater or equal to
	 */
	int value();

	/**
	 * Defines several {@link MinAge} annotations on the same element.
	 *
	 * @see MinAge
	 */
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@Retention(RUNTIME)
	@Documented
	@interface List {
		MinAge[] value();
	}
}
