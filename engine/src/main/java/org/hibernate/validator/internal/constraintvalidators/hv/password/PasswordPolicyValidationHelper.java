/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

class PasswordPolicyValidationHelper {

	private PasswordPolicyValidationHelper() {
	}

	static char[] toCharArray(CharSequence cs) {
		char[] chars = new char[cs.length()];
		for ( int i = 0; i < cs.length(); i++ ) {
			chars[i] = cs.charAt( i );
		}
		return chars;
	}

	static boolean validate(char[] password, List<PasswordPolicyRule> rules, ConstraintValidatorContext context) {
		HibernateConstraintValidatorContext hvContext = context.unwrap( HibernateConstraintValidatorContext.class );

		boolean allValid = true;
		for ( PasswordPolicyRule rule : rules ) {
			if ( !rule.isValid( password, hvContext ) ) {
				if ( allValid ) {
					hvContext.disableDefaultConstraintViolation();
					allValid = false;
				}
				hvContext.buildConstraintViolationWithTemplate( rule.getMessage() )
						.addConstraintViolation();
			}
		}

		return allValid;
	}
}
