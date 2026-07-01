/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class WhitespaceRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.noWhitespace.message}";

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		for ( char c : password ) {
			if ( Character.isWhitespace( c ) ) {
				return false;
			}
		}
		return true;
	}
}
