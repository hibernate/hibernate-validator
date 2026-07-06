/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.py;

import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.py.RUC;

/**
 * Validator for {@link RUC}.
 */
public class RUCValidator implements ConstraintValidator<RUC, CharSequence> {

	private static final Pattern UNFORMATTED_PATTERN = Pattern.compile( "[0-9A-Za-z]{3,8}[0-9]" );
	private static final Pattern FORMATTED_PATTERN = Pattern.compile( "[0-9A-Za-z]{3,8}-[0-9]" );
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
		String normalizedNumber = replaceLettersWithAscii( number );

		int total = 0;
		int multiplier = 2;
		for ( int i = normalizedNumber.length() - 1; i >= 0; i-- ) {
			if ( multiplier > BASE_MAX ) {
				multiplier = 2;
			}
			total += Character.digit( normalizedNumber.charAt( i ), 10 ) * multiplier;
			multiplier++;
		}

		int remainder = total % 11;
		if ( remainder > 1 ) {
			return 11 - remainder;
		}
		return 0;
	}

	private String replaceLettersWithAscii(String number) {
		StringBuilder normalizedNumber = new StringBuilder();
		String upperCaseNumber = number.toUpperCase( Locale.ROOT );
		for ( int i = 0; i < upperCaseNumber.length(); i++ ) {
			char character = upperCaseNumber.charAt( i );
			if ( Character.isDigit( character ) ) {
				normalizedNumber.append( character );
			}
			else {
				normalizedNumber.append( (int) character );
			}
		}
		return normalizedNumber.toString();
	}
}
