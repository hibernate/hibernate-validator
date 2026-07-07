/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.PasswordStrength.List;
import org.hibernate.validator.spi.password.PasswordStrengthScore;

/**
 * Validates that the annotated password meets a minimum strength score
 * as determined by a {@link org.hibernate.validator.spi.password.PasswordStrengthEstimator}.
 * <p>
 * The supported types are {@code CharSequence} and {@code char[]}. {@code null} is considered valid.
 * <p>
 * A {@link org.hibernate.validator.spi.password.PasswordStrengthEstimator} must be registered as a
 * validation service before using this constraint.
 *
 * @since 9.2.0
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface PasswordStrength {

	String message() default "{org.hibernate.validator.constraints.PasswordStrength.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * The minimum strength score required. Passwords with a score below this value
	 * will be considered invalid.
	 *
	 * @return the minimum score
	 * @see PasswordStrengthScore
	 */
	int min() default PasswordStrengthScore.FAIR;

	/**
	 * A bean reference to the {@link org.hibernate.validator.spi.password.PasswordStrengthEstimator}
	 * to use for strength estimation.
	 * <p>
	 * If empty (default), the single registered estimator is used. If multiple estimators are registered,
	 * a reference must be specified to avoid ambiguity.
	 *
	 * @return the bean reference string, or empty to resolve the single registered estimator
	 * @see org.hibernate.validator.bean.BeanReference
	 */
	String estimator() default "";

	/**
	 * Defines several {@code @PasswordStrength} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		PasswordStrength[] value();
	}
}
