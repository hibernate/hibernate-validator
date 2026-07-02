/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.KeyboardLayout;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

/**
 * Rejects passwords that contain keyboard walks — sequences of characters typed
 * by walking across physically adjacent keys on a keyboard.
 * <p>
 * Supports multiple keyboard layouts. When several layouts are provided, two characters
 * are considered adjacent if any layout considers them adjacent.
 */
class KeyboardWalkRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.noKeyboardWalk.message}";

	static final int DEFAULT_MIN_WALK_LENGTH = 4;

	private final int minWalkLength;
	private final KeyboardLayout[] layouts;

	KeyboardWalkRule(int minWalkLength, KeyboardLayout... layouts) {
		this.minWalkLength = minWalkLength;
		this.layouts = layouts.length > 0 ? layouts : new KeyboardLayout[] { KeyboardLayout.QWERTY };
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
		char[] password = passwordContext.password();
		if ( password.length < minWalkLength ) {
			return true;
		}

		int walkLength = 0;
		char prev = 0;

		for ( char c : password ) {
			if ( prev != 0 && isAdjacentOnAnyLayout( prev, c ) ) {
				walkLength++;
			}
			else {
				walkLength = 1;
			}
			if ( walkLength >= minWalkLength ) {
				context.addMessageParameter( "min", minWalkLength );
				return false;
			}
			prev = c;
		}

		return true;
	}

	private boolean isAdjacentOnAnyLayout(char a, char b) {
		for ( KeyboardLayout layout : layouts ) {
			if ( layout.areAdjacent( a, b ) ) {
				return true;
			}
		}
		return false;
	}
}
