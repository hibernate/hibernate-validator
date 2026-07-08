/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Named constants for common password strength score thresholds.
 * <p>
 * These values align with the 0–4 scoring scale used by libraries
 * such as zxcvbn and nbvcxz.
 *
 * @since 9.2.0
 */
@Incubating
public final class PasswordStrengthScore {

	public static final int VERY_WEAK = 0;
	public static final int WEAK = 1;
	public static final int FAIR = 2;
	public static final int STRONG = 3;
	public static final int VERY_STRONG = 4;

	private PasswordStrengthScore() {
	}
}
