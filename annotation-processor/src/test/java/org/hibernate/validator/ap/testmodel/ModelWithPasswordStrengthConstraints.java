/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.PasswordStrength;

public class ModelWithPasswordStrengthConstraints {

	@PasswordStrength
	private String string;

	@PasswordStrength
	private char[] charArray;

	@PasswordStrength
	private Integer integer;
}
