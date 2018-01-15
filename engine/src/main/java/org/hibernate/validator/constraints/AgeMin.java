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
 * @since 6.0.8
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(AgeMin.List.class)
@Documented
@Constraint(validatedBy = {})
public @interface AgeMin {

	String message() default "{org.hibernate.validator.constraints.AgeMin.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return value the age in years from a given date must be greater or equal to
	 */
	int value();

	/**
	 * Specifies whether the specified value is inclusive or exclusive.
	 * By default, it is inclusive.
	 *
	 * @return {@code true} if the number of years from a given date must be higher or equal to the specified value,
	 *         {@code false} if the number of years from a given date must be higher
	 */
	boolean inclusive() default true;

	/**
	 * Defines several {@link AgeMin} annotations on the same element.
	 *
	 * @see AgeMin
	 */
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@Retention(RUNTIME)
	@Documented
	@interface List {
		AgeMin[] value();
	}
}
