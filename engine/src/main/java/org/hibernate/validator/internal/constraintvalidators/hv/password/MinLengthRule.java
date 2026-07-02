/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class MinLengthRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.minLength.message}";

	private final int min;

	MinLengthRule(int min) {
		this.min = min;
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		char[] password = passwordContext.password();
		if ( password.length >= min ) {
			return true;
		}
		context.addMessageParameter( "min", min );
		context.addMessageParameter( "length", password.length );
		return false;
	}
}
