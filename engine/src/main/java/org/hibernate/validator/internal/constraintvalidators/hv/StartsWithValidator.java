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
 * Checks that the character sequence starts with at least one of the specified prefixes.
 *
 * @author Koen Aers
 */
public class StartsWithValidator implements ConstraintValidator<StartsWith, CharSequence> {

	private String[] values;
	private boolean ignoreCase;

	@Override
	public void initialize(StartsWith parameters) {
		this.ignoreCase = parameters.ignoreCase();

		String[] rawValues = parameters.value();
		if ( ignoreCase ) {
			this.values = new String[rawValues.length];
			for ( int i = 0; i < rawValues.length; i++ ) {
				this.values[i] = rawValues[i].toLowerCase( Locale.ROOT );
			}
		}
		else {
			this.values = rawValues;
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		String str = ignoreCase ? value.toString().toLowerCase( Locale.ROOT ) : value.toString();
		for ( String prefix : values ) {
			if ( str.startsWith( prefix ) ) {
				return true;
			}
		}
		return false;
	}
}
