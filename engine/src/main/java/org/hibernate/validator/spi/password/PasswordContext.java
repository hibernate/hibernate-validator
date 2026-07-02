/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Provides the password and optional named properties to {@link PasswordPolicyRule} implementations
 * during validation.
 * <p>
 * For char-typed field-level {@link org.hibernate.validator.constraints.PasswordPolicy @PasswordPolicy} usage,
 * the context contains only the password. For class-level usage via
 * {@link AbstractPasswordPolicyValidator}, additional properties (such as a username) can be
 * bound through {@link AbstractPasswordPolicyValidator#bindProperties(Object, PasswordContext)}.
 *
 * @since 9.2.0
 */
@Incubating
public interface PasswordContext {

	/**
	 * Returns the password being validated.
	 *
	 * @return the password as a character array (never {@code null})
	 */
	char[] password();

	/**
	 * Returns a named property that was bound to this context, or {@code null}
	 * if no property with the given name exists or the type does not match.
	 *
	 * @param name the property name
	 * @param type the expected type
	 * @param <T> the property type
	 * @return the property value, or {@code null}
	 */
	<T> T get(String name, Class<T> type);

	/**
	 * Binds a named property to this context, making it available to rules via {@link #get(String, Class)}.
	 *
	 * @param name the property name
	 * @param value the property value
	 * @return this context for method chaining
	 */
	PasswordContext property(String name, Object value);
}
