/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.function.Function;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ISBN;

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

	private int length;

	private Function<String, Boolean> checkChecksumFunction;

	@Override
	public void initialize(ISBN constraintAnnotation) {
		switch ( constraintAnnotation.type() ) {
			case ISBN10:
				length = 10;
				checkChecksumFunction = this::checkChecksumISBN10;
				break;
			case ISBN13:
				length = 13;
				checkChecksumFunction = this::checkChecksumISBN13;
				break;
		}
	}

	@Override
	public boolean isValid(CharSequence isbn, ConstraintValidatorContext context) {
		if ( isbn == null ) {
			return true;
		}

		// Replace all non-digit (or !=X) chars
		String digits = NOT_DIGITS_OR_NOT_X.matcher( isbn ).replaceAll( "" );

		// Check if the length of resulting string matches the expecting one
		if ( digits.length() != length ) {
			return false;
		}

		return checkChecksumFunction.apply( digits );
	}

	/**
	 * Check the digits for ISBN 10 using algorithm from
	 * <a href="https://en.wikipedia.org/wiki/International_Standard_Book_Number#ISBN-10_check_digits">Wikipedia</a>.
	 */
	private boolean checkChecksumISBN10(String isbn) {
		int sum = 0;
		for ( int i = 0; i < isbn.length() - 1; i++ ) {
			sum += ( isbn.charAt( i ) - '0' ) * ( i + 1 );
		}
		char checkSum = isbn.charAt( 9 );
		return sum % 11 == ( checkSum == 'X' ? 10 : checkSum - '0' );
	}

	/**
	 * Check the digits for ISBN 13 using algorithm from
	 * <a href="https://en.wikipedia.org/wiki/International_Standard_Book_Number#ISBN-13_check_digit_calculation">Wikipedia</a>.
	 */
	private boolean checkChecksumISBN13(String isbn) {
		int sum = 0;
		for ( int i = 0; i < isbn.length() - 1; i++ ) {
			sum += ( isbn.charAt( i ) - '0' ) * ( i % 2 == 0 ? 1 : 3 );
		}
		char checkSum = isbn.charAt( 12 );
		return 10 - sum % 10 == ( checkSum - '0' );
	}
}
