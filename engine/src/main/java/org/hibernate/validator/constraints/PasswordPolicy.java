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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.PasswordPolicy.List;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;

/**
 * Validates that the annotated password satisfies a set of policy rules defined by
 * a {@link PasswordPolicyDefinition} implementation.
 * <p>
 * The supported types are {@code CharSequence} and {@code char[]}.
 * {@code null} is considered valid.
 * <p>
 * Each policy rule that fails produces its own constraint violation,
 * allowing users to see all policy violations at once.
 *
 * @since 9.2.0
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE, TYPE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Incubating
public @interface PasswordPolicy {

	/**
	 * The policy definition class that configures the validation rules.
	 *
	 * @return the policy definition class
	 */
	Class<? extends PasswordPolicyDefinition> value();

	String message() default "{org.hibernate.validator.constraints.PasswordPolicy.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @PasswordPolicy} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE, TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		PasswordPolicy[] value();
	}
}
