/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

/**
 * Defines a password validation policy by configuring rules on a {@link PasswordPolicyBuilder}.
 * <p>
 * Implementations are referenced from the {@link org.hibernate.validator.constraints.PasswordPolicy @PasswordPolicy}
 * annotation and resolved at validator initialization time. By default, the implementation class
 * is instantiated via its no-arg constructor. A custom {@link PasswordPolicyDefinitionResolver}
 * can be registered to support dependency injection or other instantiation strategies.
 * <p>
 * The {@link HibernateConstraintValidatorInitializationContext} provides access to registered
 * validation services (e.g. {@link PasswordStrengthEstimator}, {@link CompromisedPasswordChecker}).
 *
 * @since 9.2.0
 */
@Incubating
public interface PasswordPolicyDefinition {

	/**
	 * Configures the password policy rules.
	 *
	 * @param builder the builder to add rules to
	 * @param context the initialization context providing access to validation services
	 */
	void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context);
}
