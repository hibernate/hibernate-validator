/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Checks that a given character sequence (e.g. string) is a valid ISBN.
 *
 * @author Marko Bekhta
 */
public class ISBNValidator implements ConstraintValidator<ISBN, CharSequence> {

	/**
	 * Pattern to replace all non ISBN characters. ISBN can have digits or 'X'.
	 */
	private static Pattern NOT_DIGITS_OR_NOT_X = Pattern.compile( "[^\\dX]" );

	private ISBNValidationAlgorithm isbnValidationAlgorithm;

	@Override
	public void initialize(ISBN constraintAnnotation) {
		this.isbnValidationAlgorithm = ISBNValidationAlgorithm.from( constraintAnnotation.type() );
	}

	@Override
	public boolean isValid(CharSequence isbn, ConstraintValidatorContext context) {
		if ( isbn == null ) {
			return true;
		}

		// Replace all non-digit (or !=X) chars
		String digits = NOT_DIGITS_OR_NOT_X.matcher( isbn ).replaceAll( "" );

		// Check if the length of resulting string matches the expecting one
		if ( !isbnValidationAlgorithm.isValidLength( digits.length() ) ) {
			return false;
		}

		return isbnValidationAlgorithm.isValidChecksum( digits );
	}

	private interface ISBNValidationAlgorithm {
		boolean isValidLength(int length);

		boolean isValidChecksum(String isbn);

		static ISBNValidationAlgorithmImpl from(ISBN.Type type) {
			Contracts.assertNotNull( type );
			switch ( type ) {
				case ISBN_10:
					return ISBNValidationAlgorithmImpl.ISBN_10;
				case ISBN_13:
					return ISBNValidationAlgorithmImpl.ISBN_13;
				case ANY:
				default:
					return ISBNValidationAlgorithmImpl.ANY;
			}
		}
	}

	private enum ISBNValidationAlgorithmImpl implements ISBNValidationAlgorithm {

		ISBN_10 {
			@Override
			public boolean isValidChecksum(String isbn) {
				return checkChecksumISBN10( isbn );
			}

			@Override
			public boolean isValidLength(int length) {
				return 10 == length;
			}
		},
		ISBN_13 {
			@Override
			public boolean isValidChecksum(String isbn) {
				return checkChecksumISBN13( isbn );
			}

			@Override
			public boolean isValidLength(int length) {
				return 13 == length;
			}
		},
		ANY {
			@Override
			public boolean isValidLength(int length) {
				return 10 == length || 13 == length;
			}

			@Override
			public boolean isValidChecksum(String isbn) {
				int length = isbn.length();
				if ( length == 10 ) {
					return checkChecksumISBN10( isbn );
				}
				else if ( length == 13 ) {
					return checkChecksumISBN13( isbn );
				}
				throw new IllegalStateException( "Invalid/unsupported isbn value length" );
			}
		};

		/**
		 * Check the digits for ISBN 10 using algorithm from
		 * <a href="https://en.wikipedia.org/wiki/International_Standard_Book_Number#ISBN-10_check_digits">Wikipedia</a>.
		 */
		private static boolean checkChecksumISBN10(String isbn) {
			int sum = 0;
			for ( int i = 0; i < isbn.length() - 1; i++ ) {
				sum += ( isbn.charAt( i ) - '0' ) * ( 10 - i );
			}
			sum += isbn.charAt( 9 ) == 'X' ? 10 : isbn.charAt( 9 ) - '0';

			return ( sum % 11 ) == 0;
		}

		/**
		 * Check the digits for ISBN 13 using algorithm from
		 * <a href="https://en.wikipedia.org/wiki/International_Standard_Book_Number#ISBN-13_check_digit_calculation">Wikipedia</a>.
		 */
		private static boolean checkChecksumISBN13(String isbn) {
			int sum = 0;
			for ( int i = 0; i < isbn.length(); i++ ) {
				sum += ( isbn.charAt( i ) - '0' ) * ( i % 2 == 0 ? 1 : 3 );
			}

			return ( sum % 10 ) == 0;
		}
	}
}
