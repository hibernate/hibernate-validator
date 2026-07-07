/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.py;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.py.RUC;

/**
 * Validator for {@link RUC}.
 */
public class RUCValidator implements ConstraintValidator<RUC, CharSequence> {

	private static final Pattern UNFORMATTED_PATTERN = Pattern.compile( "[0-9A-Za-z]{3,8}\\d" );
	private static final Pattern FORMATTED_PATTERN = Pattern.compile( "[0-9A-Za-z]{3,8}-\\d" );
	private static final int BASE_MAX = 11;

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String ruc = value.toString();
		String number;
		char checkDigit;
		if ( FORMATTED_PATTERN.matcher( ruc ).matches() ) {
			int separatorIndex = ruc.length() - 2;
			number = ruc.substring( 0, separatorIndex );
			checkDigit = ruc.charAt( ruc.length() - 1 );
		}
		else if ( UNFORMATTED_PATTERN.matcher( ruc ).matches() ) {
			number = ruc.substring( 0, ruc.length() - 1 );
			checkDigit = ruc.charAt( ruc.length() - 1 );
		}
		else {
			return false;
		}

		int calculatedCheckDigit = calculateCheckDigit( number );
		return calculatedCheckDigit == Character.digit( checkDigit, 10 );
	}

	private int calculateCheckDigit(String number) {
		int total = 0;
		int multiplier = 2;
		for ( int i = number.length() - 1; i >= 0; i-- ) {
			char character = Character.toUpperCase( number.charAt( i ) );
			if ( Character.isDigit( character ) ) {
				total += Character.digit( character, 10 ) * multiplier;
				multiplier = increaseMultiplier( multiplier );
			}
			else {
				int asciiValue = character;
				total += ( asciiValue % 10 ) * multiplier;
				multiplier = increaseMultiplier( multiplier );
				total += ( asciiValue / 10 ) * multiplier;
				multiplier = increaseMultiplier( multiplier );
			}
		}

		int remainder = total % 11;
		if ( remainder > 1 ) {
			return 11 - remainder;
		}
		return 0;
	}

	private int increaseMultiplier(int multiplier) {
		if ( multiplier == BASE_MAX ) {
			return 2;
		}
		return multiplier + 1;
	}
}
