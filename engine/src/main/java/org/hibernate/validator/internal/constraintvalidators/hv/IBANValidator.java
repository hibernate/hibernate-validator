/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.IBAN;

/**
 * Checks that a given character sequence (e.g. string) is a valid IBAN (International Bank Account Number).
 * <p>
 * Validation is performed by checking the country-specific length and the ISO 7064 MOD 97-10 check digits.
 *
 * @author Andrea Boriero
 */
public class IBANValidator implements ConstraintValidator<IBAN, CharSequence> {

	private static final int MODULUS = 97;

	/**
	 * The general IBAN structure: two letters (country code), two check digits and up to 30 alphanumeric
	 * characters (BBAN). The country-specific length is verified separately.
	 */
	private static final Pattern IBAN_STRUCTURE = Pattern.compile( "[A-Za-z]{2}[0-9]{2}[A-Za-z0-9]+" );

	/**
	 * The expected total IBAN length per country, as defined by the SWIFT IBAN registry.
	 */
	private static final Map<String, Integer> IBAN_COUNTRY_LENGTHS = buildCountryLengths();

	private boolean allowLowercase;

	@Override
	public void initialize(IBAN parameters) {
		allowLowercase = parameters.allowLowercase();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		if ( !allowLowercase && !isUpperCase( value ) ) {
			return false;
		}

		// Spaces are used to group characters when printing an IBAN, they are not part of the actual number.
		final String iban = removeSpaces( value );

		if ( !IBAN_STRUCTURE.matcher( iban ).matches() ) {
			return false;
		}

		// Reject unknown country codes and any IBAN whose length does not match the fixed length defined for its country.
		final Integer expectedLength = IBAN_COUNTRY_LENGTHS.get( iban.substring( 0, 2 ).toUpperCase( Locale.ROOT ) );
		if ( expectedLength == null || expectedLength != iban.length() ) {
			return false;
		}

		return hasValidCheckDigits( iban );
	}

	private static String removeSpaces(CharSequence value) {
		return value.toString().replace( " ", "" );
	}

	/**
	 * Validates the ISO 7064 MOD 97-10 check digits: the first four characters are moved to the end,
	 * each letter is replaced by two digits ('A' = 10, ..., 'Z' = 35) and the resulting number must
	 * yield a remainder of 1 when divided by 97. The remainder is computed piece by piece to avoid
	 * building a potentially very large integer.
	 */
	private static boolean hasValidCheckDigits(String iban) {
		int length = iban.length();
		int mod = 0;
		for ( int i = 0; i < length; i++ ) {
			// Start with the BBAN (chars after the first four), then wrap around to the country code and check digits.
			char c = iban.charAt( ( i + 4 ) % length );
			if ( c >= '0' && c <= '9' ) {
				mod = ( mod * 10 + ( c - '0' ) ) % MODULUS;
			}
			else {
				mod = ( mod * 100 + ( Character.toUpperCase( c ) - 'A' + 10 ) ) % MODULUS;
			}
		}
		return mod == 1;
	}

	private boolean isUpperCase(CharSequence value) {
		for ( int i = 0; i < value.length(); i++ ) {
			char c = value.charAt( i );
			if ( Character.isLetter( c ) && !Character.isUpperCase( c ) ) {
				return false;
			}
		}
		return true;
	}

	private static Map<String, Integer> buildCountryLengths() {
		Map<String, Integer> lengths = new HashMap<>();
		lengths.put( "AD", 24 );
		lengths.put( "AE", 23 );
		lengths.put( "AL", 28 );
		lengths.put( "AT", 20 );
		lengths.put( "AZ", 28 );
		lengths.put( "BA", 20 );
		lengths.put( "BE", 16 );
		lengths.put( "BG", 22 );
		lengths.put( "BH", 22 );
		lengths.put( "BR", 29 );
		lengths.put( "BY", 28 );
		lengths.put( "CH", 21 );
		lengths.put( "CR", 22 );
		lengths.put( "CY", 28 );
		lengths.put( "CZ", 24 );
		lengths.put( "DE", 22 );
		lengths.put( "DK", 18 );
		lengths.put( "DO", 28 );
		lengths.put( "EE", 20 );
		lengths.put( "EG", 29 );
		lengths.put( "ES", 24 );
		lengths.put( "FI", 18 );
		lengths.put( "FO", 18 );
		lengths.put( "FR", 27 );
		lengths.put( "GB", 22 );
		lengths.put( "GE", 22 );
		lengths.put( "GI", 23 );
		lengths.put( "GL", 18 );
		lengths.put( "GR", 27 );
		lengths.put( "GT", 28 );
		lengths.put( "HR", 21 );
		lengths.put( "HU", 28 );
		lengths.put( "IE", 22 );
		lengths.put( "IL", 23 );
		lengths.put( "IQ", 23 );
		lengths.put( "IS", 26 );
		lengths.put( "IT", 27 );
		lengths.put( "JO", 30 );
		lengths.put( "KW", 30 );
		lengths.put( "KZ", 20 );
		lengths.put( "LB", 28 );
		lengths.put( "LC", 32 );
		lengths.put( "LI", 21 );
		lengths.put( "LT", 20 );
		lengths.put( "LU", 20 );
		lengths.put( "LV", 21 );
		lengths.put( "LY", 25 );
		lengths.put( "MC", 27 );
		lengths.put( "MD", 24 );
		lengths.put( "ME", 22 );
		lengths.put( "MK", 19 );
		lengths.put( "MR", 27 );
		lengths.put( "MT", 31 );
		lengths.put( "MU", 30 );
		lengths.put( "NL", 18 );
		lengths.put( "NO", 15 );
		lengths.put( "PK", 24 );
		lengths.put( "PL", 28 );
		lengths.put( "PS", 29 );
		lengths.put( "PT", 25 );
		lengths.put( "QA", 29 );
		lengths.put( "RO", 24 );
		lengths.put( "RS", 22 );
		lengths.put( "SA", 24 );
		lengths.put( "SC", 31 );
		lengths.put( "SD", 18 );
		lengths.put( "SE", 24 );
		lengths.put( "SI", 19 );
		lengths.put( "SK", 24 );
		lengths.put( "SM", 27 );
		lengths.put( "ST", 25 );
		lengths.put( "SV", 28 );
		lengths.put( "TL", 23 );
		lengths.put( "TN", 24 );
		lengths.put( "TR", 26 );
		lengths.put( "UA", 29 );
		lengths.put( "VA", 22 );
		lengths.put( "VG", 24 );
		lengths.put( "XK", 20 );
		return Collections.unmodifiableMap( lengths );
	}
}
