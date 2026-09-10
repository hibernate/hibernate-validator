/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ISSN;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Checks that a given character sequence (e.g. string) is a valid ISSN.
 *
 * @author Andrea Boriero
 */
public class ISSNValidator implements ConstraintValidator<ISSN, CharSequence> {

	/**
	 * Pattern to replace all non ISSN characters. ISSN can have digits or 'X'.
	 */
	private static Pattern NOT_DIGITS_OR_NOT_X = Pattern.compile( "[^\\dX]" );

	private ISSNValidationAlgorithm issnValidationAlgorithm;

	@Override
	public void initialize(ISSN constraintAnnotation) {
		this.issnValidationAlgorithm = ISSNValidationAlgorithm.from( constraintAnnotation.type() );
	}

	@Override
	public boolean isValid(CharSequence issn, ConstraintValidatorContext context) {
		if ( issn == null ) {
			return true;
		}

		// Replace all non-digit (or !=X) chars
		String digits = NOT_DIGITS_OR_NOT_X.matcher( issn ).replaceAll( "" );

		// Check if the length of resulting string matches the expecting one
		if ( !issnValidationAlgorithm.isValidLength( digits.length() ) ) {
			return false;
		}

		return issnValidationAlgorithm.isValidChecksum( digits );
	}

	private interface ISSNValidationAlgorithm {
		boolean isValidLength(int length);

		boolean isValidChecksum(String issn);

		static ISSNValidationAlgorithmImpl from(ISSN.Type type) {
			Contracts.assertNotNull( type );
			switch ( type ) {
				case ISSN_8:
					return ISSNValidationAlgorithmImpl.ISSN_8;
				case ISSN_13:
					return ISSNValidationAlgorithmImpl.ISSN_13;
				case ANY:
				default:
					return ISSNValidationAlgorithmImpl.ANY;
			}
		}
	}

	private enum ISSNValidationAlgorithmImpl implements ISSNValidationAlgorithm {

		ISSN_8 {
			@Override
			public boolean isValidChecksum(String issn) {
				return checkChecksumISSN8( issn );
			}

			@Override
			public boolean isValidLength(int length) {
				return 8 == length;
			}
		},
		ISSN_13 {
			@Override
			public boolean isValidChecksum(String issn) {
				return checkChecksumISSN13( issn );
			}

			@Override
			public boolean isValidLength(int length) {
				return 13 == length;
			}
		},
		ANY {
			@Override
			public boolean isValidLength(int length) {
				return 8 == length || 13 == length;
			}

			@Override
			public boolean isValidChecksum(String issn) {
				int length = issn.length();
				if ( length == 8 ) {
					return checkChecksumISSN8( issn );
				}
				else if ( length == 13 ) {
					return checkChecksumISSN13( issn );
				}
				throw new IllegalStateException( "Invalid/unsupported issn value length" );
			}
		};

		/**
		 * Check the digits for ISSN-8 using modulus 11 algorithm with weights 8 to 1.
		 * The check digit calculation: sum of (digit × position weight from left, 8 to 2) mod 11.
		 * If remainder is 0, check digit is 0. Otherwise check digit is 11 - remainder.
		 * When check digit would be 10, it's represented as 'X'.
		 * For validation, we verify that the sum of all 8 positions (with weights 8 to 1) mod 11 equals 0.
		 */
		private static boolean checkChecksumISSN8(String issn) {
			int sum = 0;
			for ( int i = 0; i < issn.length(); i++ ) {
				int digit = issn.charAt( i ) == 'X' ? 10 : issn.charAt( i ) - '0';
				sum += digit * ( 8 - i );
			}

			return ( sum % 11 ) == 0;
		}

		/**
		 * Check the digits for ISSN-13 (EAN-13 format) using modulus 10 algorithm.
		 * ISSN-13 format: 977 prefix + 7 ISSN digits (excluding ISSN-8 check digit) + 2 sequence digits + EAN check digit.
		 * The algorithm alternates multiplying by 1 and 3 from left to right.
		 */
		private static boolean checkChecksumISSN13(String issn) {
			// Verify 977 prefix for ISSN in EAN-13 format
			if ( !issn.startsWith( "977" ) ) {
				return false;
			}

			int sum = 0;
			for ( int i = 0; i < issn.length(); i++ ) {
				sum += ( issn.charAt( i ) - '0' ) * ( i % 2 == 0 ? 1 : 3 );
			}

			return ( sum % 10 ) == 0;
		}
	}
}
