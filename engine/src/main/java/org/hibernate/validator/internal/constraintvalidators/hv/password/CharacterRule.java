/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.Locale;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class CharacterRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.requireCharacters.message}";

	private final CharacterType type;
	private final int min;

	CharacterRule(CharacterType type, int min) {
		this.type = type;
		this.min = min;
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		int count = 0;
		for ( char c : password ) {
			if ( type.matches( c ) ) {
				count++;
			}
		}
		if ( count >= min ) {
			return true;
		}
		context.addMessageParameter( "type", type.name().toLowerCase( Locale.ROOT ) );
		context.addMessageParameter( "min", min );
		context.addMessageParameter( "count", count );
		return false;
	}
}
