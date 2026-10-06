/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.EndsWith;

/**
 * Checks that the character sequence ends with the specified suffix.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class EndsWithValidator implements ConstraintValidator<EndsWith, CharSequence> {

	private String[] suffixes;
	private boolean ignoreCase;

	@Override
	public void initialize(EndsWith parameters) {
		this.ignoreCase = parameters.ignoreCase();
		String[] rawSuffixes = parameters.value();
		if ( ignoreCase ) {
			this.suffixes = new String[rawSuffixes.length];
			for ( int i = 0; i < rawSuffixes.length; i++ ) {
				this.suffixes[i] = rawSuffixes[i].toLowerCase( Locale.ROOT );
			}
		}
		else {
			this.suffixes = rawSuffixes;
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		String str = value.toString();
		if ( ignoreCase ) {
			str = str.toLowerCase( Locale.ROOT );
		}

		for ( String suffix : suffixes ) {
			if ( str.endsWith( suffix ) ) {
				return true;
			}
		}
		return false;
	}
}
