/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.BIC;

/**
 * Checks that a given character sequence is a valid BIC (Bank Identifier Code / SWIFT code).
 * <p>
 * Validation is performed by checking the structural format per ISO 9362:
 * length (8 or 11), character set per position, and valid ISO 3166-1 alpha-2 country code.
 *
 * @author Andrea Boriero
 */
public class BICValidator implements ConstraintValidator<BIC, CharSequence> {

	/**
	 * Valid ISO 3166-1 alpha-2 country codes, plus 'XK' for Kosovo (SWIFT-specific extension).
	 */
	private static final Set<String> VALID_COUNTRY_CODES = buildValidCountryCodes();

	private boolean allowLowercase;
	private Set<String> allowedCountryCodes;
	private Set<String> allowedBankCodes;

	@Override
	public void initialize(BIC parameters) {
		this.allowLowercase = parameters.allowLowercase();

		// Normalize country codes to uppercase for case-insensitive matching
		String[] countryCodes = parameters.countryCodes();
		if ( countryCodes.length > 0 ) {
			this.allowedCountryCodes = new HashSet<>( countryCodes.length );
			for ( String code : countryCodes ) {
				if ( !VALID_COUNTRY_CODES.contains( code.toUpperCase( Locale.ROOT ) ) ) {
					throw new IllegalArgumentException( "Invalid country code: " + code );
				}
				this.allowedCountryCodes.add( code.toUpperCase( Locale.ROOT ) );
			}
		}
		else {
			this.allowedCountryCodes = Collections.emptySet();
		}

		// Normalize bank codes to uppercase for case-insensitive matching
		String[] bankCodes = parameters.bankCodes();
		if ( bankCodes.length > 0 ) {
			this.allowedBankCodes = new HashSet<>( bankCodes.length );
			for ( String code : bankCodes ) {
				this.allowedBankCodes.add( code.toUpperCase( Locale.ROOT ) );
			}
		}
		else {
			this.allowedBankCodes = Collections.emptySet();
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String bic = value.toString();

		// Length must be exactly 8 (BIC8) or 11 (BIC11)
		int length = bic.length();
		if ( length != 8 && length != 11 ) {
			return false;
		}

		// Case sensitivity check
		if ( !allowLowercase && !isUpperCase( bic ) ) {
			return false;
		}

		// Positions 1-4: Institution (bank) code - must be letters only
		// Positions 5-6: Country code - must be letters only and a valid ISO 3166-1 alpha-2 code
		if ( !isLetters( bic, 0, 6 ) ) {
			return false;
		}

		String countryCode = bic.substring( 4, 6 ).toUpperCase( Locale.ROOT );
		if ( !VALID_COUNTRY_CODES.contains( countryCode ) ) {
			return false;
		}

		// Positions 7-8: Location code - must be alphanumeric
		if ( !isAlphanumeric( bic, 6, 8 ) ) {
			return false;
		}

		// Positions 9-11: Branch code - must be alphanumeric (if present)
		if ( length == 11 && !isAlphanumeric( bic, 8, 11 ) ) {
			return false;
		}

		// Optional filtering by country code
		if ( !allowedCountryCodes.isEmpty() && !allowedCountryCodes.contains( countryCode ) ) {
			return false;
		}

		// Optional filtering by bank code
		if ( !allowedBankCodes.isEmpty() ) {
			if ( !allowedBankCodes.contains( bic.substring( 0, 4 ).toUpperCase( Locale.ROOT ) ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if all characters in the given range are uppercase letters or non-letters.
	 */
	private boolean isUpperCase(String str) {
		for ( int i = 0; i < str.length(); i++ ) {
			char c = str.charAt( i );
			if ( Character.isLetter( c ) && !Character.isUpperCase( c ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all characters in the range [start, end) are letters.
	 */
	private boolean isLetters(String str, int start, int end) {
		for ( int i = start; i < end; i++ ) {
			char c = str.charAt( i );
			if ( !Character.isLetter( c ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all characters in the range [start, end) are alphanumeric (letters or digits).
	 */
	private boolean isAlphanumeric(String str, int start, int end) {
		for ( int i = start; i < end; i++ ) {
			char c = str.charAt( i );
			if ( !Character.isLetterOrDigit( c ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Builds the set of valid ISO 3166-1 alpha-2 country codes, plus SWIFT's 'XK' for Kosovo.
	 */
	private static Set<String> buildValidCountryCodes() {
		// Get all ISO 3166-1 alpha-2 country codes from the JDK
		Set<String> codes = new HashSet<>( Locale.getISOCountries( Locale.IsoCountryCode.PART1_ALPHA2 ) );

		// SWIFT-specific extension for Kosovo (not in ISO 3166-1)
		codes.add( "XK" );

		return Collections.unmodifiableSet( codes );
	}
}
