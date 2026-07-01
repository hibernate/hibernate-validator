/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class CompromisedRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.compromisedChecker.message}";

	private final CompromisedPasswordChecker checker;

	CompromisedRule(CompromisedPasswordChecker checker) {
		this.checker = checker;
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		CompromisedPasswordResult result = checker.check( password );

		if ( !result.compromised() ) {
			return true;
		}

		if ( result.occurrences() >= 0 ) {
			context.addMessageParameter( "occurrences", result.occurrences() );
		}

		return false;
	}
}
