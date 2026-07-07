/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Checks whether a password has been compromised (e.g. found in a data breach).
 * <p>
 * Can be registered as a bean via a
 * {@link org.hibernate.validator.spi.bean.BeanConfigurer}.
 *
 * @since 9.2.0
 */
@Incubating
public interface CompromisedPasswordChecker {

	/**
	 * Checks whether the given password is compromised.
	 *
	 * @param password the password to check (never {@code null})
	 * @return the check result
	 */
	CompromisedPasswordResult check(char[] password);
}
