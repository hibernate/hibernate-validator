/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class RepeatingCharactersRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.noRepeatingCharacters.message}";

	private final int maxConsecutive;

	RepeatingCharactersRule(int maxConsecutive) {
		this.maxConsecutive = maxConsecutive;
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		if ( password.length <= maxConsecutive ) {
			return true;
		}
		int count = 1;
		for ( int i = 1; i < password.length; i++ ) {
			if ( password[i] == password[i - 1] ) {
				count++;
				if ( count > maxConsecutive ) {
					context.addMessageParameter( "max", maxConsecutive );
					return false;
				}
			}
			else {
				count = 1;
			}
		}
		return true;
	}
}
