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
		Set<String> codes = new HashSet<>();
		// ISO 3166-1 alpha-2 country codes
		codes.add( "AD" );
		codes.add( "AE" );
		codes.add( "AF" );
		codes.add( "AG" );
		codes.add( "AI" );
		codes.add( "AL" );
		codes.add( "AM" );
		codes.add( "AO" );
		codes.add( "AQ" );
		codes.add( "AR" );
		codes.add( "AS" );
		codes.add( "AT" );
		codes.add( "AU" );
		codes.add( "AW" );
		codes.add( "AX" );
		codes.add( "AZ" );
		codes.add( "BA" );
		codes.add( "BB" );
		codes.add( "BD" );
		codes.add( "BE" );
		codes.add( "BF" );
		codes.add( "BG" );
		codes.add( "BH" );
		codes.add( "BI" );
		codes.add( "BJ" );
		codes.add( "BL" );
		codes.add( "BM" );
		codes.add( "BN" );
		codes.add( "BO" );
		codes.add( "BQ" );
		codes.add( "BR" );
		codes.add( "BS" );
		codes.add( "BT" );
		codes.add( "BV" );
		codes.add( "BW" );
		codes.add( "BY" );
		codes.add( "BZ" );
		codes.add( "CA" );
		codes.add( "CC" );
		codes.add( "CD" );
		codes.add( "CF" );
		codes.add( "CG" );
		codes.add( "CH" );
		codes.add( "CI" );
		codes.add( "CK" );
		codes.add( "CL" );
		codes.add( "CM" );
		codes.add( "CN" );
		codes.add( "CO" );
		codes.add( "CR" );
		codes.add( "CU" );
		codes.add( "CV" );
		codes.add( "CW" );
		codes.add( "CX" );
		codes.add( "CY" );
		codes.add( "CZ" );
		codes.add( "DE" );
		codes.add( "DJ" );
		codes.add( "DK" );
		codes.add( "DM" );
		codes.add( "DO" );
		codes.add( "DZ" );
		codes.add( "EC" );
		codes.add( "EE" );
		codes.add( "EG" );
		codes.add( "EH" );
		codes.add( "ER" );
		codes.add( "ES" );
		codes.add( "ET" );
		codes.add( "FI" );
		codes.add( "FJ" );
		codes.add( "FK" );
		codes.add( "FM" );
		codes.add( "FO" );
		codes.add( "FR" );
		codes.add( "GA" );
		codes.add( "GB" );
		codes.add( "GD" );
		codes.add( "GE" );
		codes.add( "GF" );
		codes.add( "GG" );
		codes.add( "GH" );
		codes.add( "GI" );
		codes.add( "GL" );
		codes.add( "GM" );
		codes.add( "GN" );
		codes.add( "GP" );
		codes.add( "GQ" );
		codes.add( "GR" );
		codes.add( "GS" );
		codes.add( "GT" );
		codes.add( "GU" );
		codes.add( "GW" );
		codes.add( "GY" );
		codes.add( "HK" );
		codes.add( "HM" );
		codes.add( "HN" );
		codes.add( "HR" );
		codes.add( "HT" );
		codes.add( "HU" );
		codes.add( "ID" );
		codes.add( "IE" );
		codes.add( "IL" );
		codes.add( "IM" );
		codes.add( "IN" );
		codes.add( "IO" );
		codes.add( "IQ" );
		codes.add( "IR" );
		codes.add( "IS" );
		codes.add( "IT" );
		codes.add( "JE" );
		codes.add( "JM" );
		codes.add( "JO" );
		codes.add( "JP" );
		codes.add( "KE" );
		codes.add( "KG" );
		codes.add( "KH" );
		codes.add( "KI" );
		codes.add( "KM" );
		codes.add( "KN" );
		codes.add( "KP" );
		codes.add( "KR" );
		codes.add( "KW" );
		codes.add( "KY" );
		codes.add( "KZ" );
		codes.add( "LA" );
		codes.add( "LB" );
		codes.add( "LC" );
		codes.add( "LI" );
		codes.add( "LK" );
		codes.add( "LR" );
		codes.add( "LS" );
		codes.add( "LT" );
		codes.add( "LU" );
		codes.add( "LV" );
		codes.add( "LY" );
		codes.add( "MA" );
		codes.add( "MC" );
		codes.add( "MD" );
		codes.add( "ME" );
		codes.add( "MF" );
		codes.add( "MG" );
		codes.add( "MH" );
		codes.add( "MK" );
		codes.add( "ML" );
		codes.add( "MM" );
		codes.add( "MN" );
		codes.add( "MO" );
		codes.add( "MP" );
		codes.add( "MQ" );
		codes.add( "MR" );
		codes.add( "MS" );
		codes.add( "MT" );
		codes.add( "MU" );
		codes.add( "MV" );
		codes.add( "MW" );
		codes.add( "MX" );
		codes.add( "MY" );
		codes.add( "MZ" );
		codes.add( "NA" );
		codes.add( "NC" );
		codes.add( "NE" );
		codes.add( "NF" );
		codes.add( "NG" );
		codes.add( "NI" );
		codes.add( "NL" );
		codes.add( "NO" );
		codes.add( "NP" );
		codes.add( "NR" );
		codes.add( "NU" );
		codes.add( "NZ" );
		codes.add( "OM" );
		codes.add( "PA" );
		codes.add( "PE" );
		codes.add( "PF" );
		codes.add( "PG" );
		codes.add( "PH" );
		codes.add( "PK" );
		codes.add( "PL" );
		codes.add( "PM" );
		codes.add( "PN" );
		codes.add( "PR" );
		codes.add( "PS" );
		codes.add( "PT" );
		codes.add( "PW" );
		codes.add( "PY" );
		codes.add( "QA" );
		codes.add( "RE" );
		codes.add( "RO" );
		codes.add( "RS" );
		codes.add( "RU" );
		codes.add( "RW" );
		codes.add( "SA" );
		codes.add( "SB" );
		codes.add( "SC" );
		codes.add( "SD" );
		codes.add( "SE" );
		codes.add( "SG" );
		codes.add( "SH" );
		codes.add( "SI" );
		codes.add( "SJ" );
		codes.add( "SK" );
		codes.add( "SL" );
		codes.add( "SM" );
		codes.add( "SN" );
		codes.add( "SO" );
		codes.add( "SR" );
		codes.add( "SS" );
		codes.add( "ST" );
		codes.add( "SV" );
		codes.add( "SX" );
		codes.add( "SY" );
		codes.add( "SZ" );
		codes.add( "TC" );
		codes.add( "TD" );
		codes.add( "TF" );
		codes.add( "TG" );
		codes.add( "TH" );
		codes.add( "TJ" );
		codes.add( "TK" );
		codes.add( "TL" );
		codes.add( "TM" );
		codes.add( "TN" );
		codes.add( "TO" );
		codes.add( "TR" );
		codes.add( "TT" );
		codes.add( "TV" );
		codes.add( "TW" );
		codes.add( "TZ" );
		codes.add( "UA" );
		codes.add( "UG" );
		codes.add( "UM" );
		codes.add( "US" );
		codes.add( "UY" );
		codes.add( "UZ" );
		codes.add( "VA" );
		codes.add( "VC" );
		codes.add( "VE" );
		codes.add( "VG" );
		codes.add( "VI" );
		codes.add( "VN" );
		codes.add( "VU" );
		codes.add( "WF" );
		codes.add( "WS" );
		codes.add( "YE" );
		codes.add( "YT" );
		codes.add( "ZA" );
		codes.add( "ZM" );
		codes.add( "ZW" );
		// SWIFT-specific extension for Kosovo (not in ISO 3166-1)
		codes.add( "XK" );
		return Collections.unmodifiableSet( codes );
	}
}
