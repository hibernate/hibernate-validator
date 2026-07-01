/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * A single rule within a {@link PasswordPolicyDefinition password policy}.
 * <p>
 * Implementations can use the {@link HibernateConstraintValidatorContext} to add
 * message parameters for interpolation in the violation message returned by {@link #getMessage()}.
 *
 * @since 9.2.0
 * @see PasswordPolicyBuilder#addRule(PasswordPolicyRule)
 */
@Incubating
public interface PasswordPolicyRule {

	/**
	 * Returns the message template for the constraint violation produced when this rule fails.
	 * <p>
	 * May be a literal message or a message key enclosed in braces
	 * (e.g. {@code "{com.acme.password.rule.message}"}).
	 *
	 * @return the message template
	 */
	String getMessage();

	/**
	 * Validates the given password against this rule.
	 *
	 * @param password the password to validate (never {@code null})
	 * @param context the constraint validator context for adding message parameters
	 * @return {@code true} if the password satisfies this rule, {@code false} otherwise
	 */
	boolean isValid(char[] password, HibernateConstraintValidatorContext context);
}
