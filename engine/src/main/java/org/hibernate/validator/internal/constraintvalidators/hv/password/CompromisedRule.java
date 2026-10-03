/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;
import org.hibernate.validator.spi.password.PasswordContext;
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
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		CompromisedPasswordResult result = checker.check( passwordContext.password() );

		if ( !result.compromised() ) {
			return true;
		}

		result.addMessageParameters( context );

		return false;
	}
}
