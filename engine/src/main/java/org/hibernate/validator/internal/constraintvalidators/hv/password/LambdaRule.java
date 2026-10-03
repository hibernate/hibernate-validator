/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.function.Predicate;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class LambdaRule implements PasswordPolicyRule {

	private final String message;
	private final Predicate<char[]> predicate;

	LambdaRule(String message, Predicate<char[]> predicate) {
		this.message = message;
		this.predicate = predicate;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		return predicate.test( passwordContext.password() );
	}
}
