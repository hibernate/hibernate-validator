/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Character categories for password policy rules.
 *
 * @since 9.2.0
 */
@Incubating
public enum CharacterType {

	UPPERCASE {
		@Override
		public boolean matches(char c) {
			return Character.isUpperCase( c );
		}
	},

	LOWERCASE {
		@Override
		public boolean matches(char c) {
			return Character.isLowerCase( c );
		}
	},

	DIGIT {
		@Override
		public boolean matches(char c) {
			return Character.isDigit( c );
		}
	},

	SPECIAL {
		@Override
		public boolean matches(char c) {
			return !Character.isLetterOrDigit( c );
		}
	};

	/**
	 * Returns {@code true} if the given character belongs to this category.
	 */
	public abstract boolean matches(char c);
}
