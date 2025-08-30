/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Annotation to specify that a field or parameter must be one of a defined set of values.
 * This can be enforced using string, integer, float, or double values, or by restricting the values to those
 * within an enum type.
 *
 * <p> For string values, the annotation supports case-insensitive matching. </p>
 *
 * <p> Usage example: </p>
 * <pre>{@code
 * @OneOf(allowedValues = {"ACTIVE", "INACTIVE"}, ignoreCase = true)
 * private String status;
 * }</pre>
 *
 * <p>The message attribute provides a customizable error message when validation fails. The groups and payload
 * attributes allow the validation to be applied to specific validation groups or custom payload types.</p>
 *
 * <p> You can use the following fields in the annotation: </p>
 * <ul>
 *   <li><code>allowedIntegers</code>: A set of allowed integer values.</li>
 *   <li><code>allowedLongs</code>: A set of allowed long values.</li>
 *   <li><code>allowedFloats</code>: A set of allowed float values.</li>
 *   <li><code>allowedDoubles</code>: A set of allowed double values.</li>
 *   <li><code>allowedValues</code>: A set of allowed string values.</li>
 *   <li><code>enumClass</code>: The class of the enum type, if applicable.</li>
 *   <li><code>ignoreCase</code>: If true, string matching is case-insensitive.</li>
 * </ul>
 *
 * @author Yusuf Álàmù
 * @since 9.0.0
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface OneOf {

	String message() default "{org.hibernate.validator.constraints.OneOf.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	int[] allowedIntegers() default { };

	long[] allowedLongs() default { };

	float[] allowedFloats() default { };

	double[] allowedDoubles() default { };

	String[] allowedValues() default { };

	Class<? extends Enum<?>> enumClass() default DefaultEnum.class;

	boolean ignoreCase() default false;

	enum DefaultEnum {
	}
}
