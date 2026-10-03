/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class IllegalCharactersRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.illegalCharacters.message}";

	private final Set<Character> illegal;

	IllegalCharactersRule(char[] illegalChars) {
		this.illegal = new HashSet<>();
		for ( char c : illegalChars ) {
			this.illegal.add( c );
		}
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		for ( char c : passwordContext.password() ) {
			if ( illegal.contains( c ) ) {
				return false;
			}
		}
		return true;
	}
}
