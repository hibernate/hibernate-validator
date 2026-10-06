/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.StartsWith;

/**
 * Checks that the character sequence starts with the specified prefix.
 *
 * @author Andrea Boriero
 * @author Koen Aers
 * @since 9.2
 */
public class StartsWithValidator implements ConstraintValidator<StartsWith, CharSequence> {

	private String[] prefixes;
	private boolean ignoreCase;

	@Override
	public void initialize(StartsWith parameters) {
		this.ignoreCase = parameters.ignoreCase();
		String[] rawPrefixes = parameters.value();
		if ( ignoreCase ) {
			this.prefixes = new String[rawPrefixes.length];
			for ( int i = 0; i < rawPrefixes.length; i++ ) {
				this.prefixes[i] = rawPrefixes[i].toLowerCase( Locale.ROOT );
			}
		}
		else {
			this.prefixes = rawPrefixes;
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

		for ( String prefix : prefixes ) {
			if ( str.startsWith( prefix ) ) {
				return true;
			}
		}
		return false;
	}
}
