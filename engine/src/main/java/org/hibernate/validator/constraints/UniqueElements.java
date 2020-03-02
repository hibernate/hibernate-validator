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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.UniqueElements.List;

/**
 * Validates that every object in the provided {@link Collection} is unique, i.e. that we can't find 2 equal elements in
 * the collection.
 * <p>
 * For instance, this can be useful with JAX-RS, which always deserializes collections to a list. Thus, duplicates would
 * implicitly and silently be removed when converting it to a set. This constraint allows you to check for duplicates in
 * the list and to raise an error instead.
 *
 * @author Tadhg Pearson
 * @since 6.0.5
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface UniqueElements {

	String message() default "{org.hibernate.validator.constraints.UniqueElements.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * Defines several {@code @UniqueElements} annotations on the same element.
	 */
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		UniqueElements[] value();
	}
}
