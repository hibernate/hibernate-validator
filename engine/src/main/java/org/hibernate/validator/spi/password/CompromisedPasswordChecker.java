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
 * <p>
 * @hv.securityNote Implementations must handle password data securely.
 * Check with the most current rules and standards for handling passwords.
 * Following are a few examples of what to check for (applicable at the time of writing this documentation):
 * <ul>
 *     <li>Never send plaintext passwords or complete hashes to external services — use techniques
 *     such as k-anonymity (e.g. sending only a hash prefix).</li>
 *     <li>Never log passwords.</li>
 *     <li>The {@code char[]} array should not be stored beyond the scope of the {@link #check(char[])} call.</li>
 * </ul>
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
