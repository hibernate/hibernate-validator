/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Estimates the strength of a password.
 * <p>
 * Implementations can delegate to libraries such as
 * <a href="https://github.com/GoSimpleLLC/nbvcxz">nbvcxz</a> or
 * <a href="https://github.com/nulab/zxcvbn4j">zxcvbn4j</a>.
 * <p>
 * Can be registered as a bean via a
 * {@link org.hibernate.validator.spi.bean.BeanConfigurer}.
 *
 * @since 9.2.0
 */
@Incubating
public interface PasswordStrengthEstimator {

	/**
	 * Estimates the strength of the given password.
	 *
	 * @param password the password to evaluate (never {@code null})
	 * @return the estimation result
	 */
	PasswordStrengthResult estimate(char[] password);

	/**
	 * Estimates the strength of the given password.
	 *
	 * @param password the password to evaluate (never {@code null})
	 * @return the estimation result
	 */
	default PasswordStrengthResult estimate(String password) {
		return estimate( password.toCharArray() );
	}
}
