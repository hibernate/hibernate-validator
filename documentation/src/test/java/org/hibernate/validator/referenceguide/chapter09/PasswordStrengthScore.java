/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

public final class PasswordStrengthScore {

	public static final int VERY_WEAK = 0;
	public static final int WEAK = 1;
	public static final int FAIR = 2;
	public static final int STRONG = 3;
	public static final int VERY_STRONG = 4;

	private PasswordStrengthScore() {
	}
}
