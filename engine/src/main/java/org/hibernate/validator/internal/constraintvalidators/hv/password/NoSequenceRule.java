/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class NoSequenceRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.noSequence.message}";
	private static final int MIN_SEQUENCE_LENGTH = 3;

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		char[] password = passwordContext.password();
		if ( password.length < MIN_SEQUENCE_LENGTH ) {
			return true;
		}

		int previousDelta = 0;

		for ( int i = 1; i < password.length; i++ ) {
			int delta = password[i] - password[i - 1];

			if ( ( delta == 1 || delta == -1 ) && delta == previousDelta ) {
				return false;
			}
			previousDelta = delta;
		}

		return true;
	}
}
