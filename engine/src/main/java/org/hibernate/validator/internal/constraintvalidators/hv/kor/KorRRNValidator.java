
/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.kor;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.kor.KorRRN;

public class KorRRNValidator implements ConstraintValidator<KorRRN, CharSequence> {

	private static final Pattern REGEX = Pattern.compile( "\\d{6}-\\d{7}" );

	private static final int CHECK_DIGIT_INDEX = 13;
	private static final int[] CHECK_DIGIT_ARRAY = { 2, 3, 4, 5, 6, 7, -1, 8, 9, 2, 3, 4, 5 };

	@Override
	public boolean isValid(CharSequence rrnValue, ConstraintValidatorContext context) {
		if ( rrnValue == null ) {
			return true;
		}
		String rrn = rrnValue.toString();

		if ( !isValidFormat( rrn ) ) {
			return false;
		}

		int month = Integer.parseInt( rrn.substring( 2, 4 ) );
		int day = Integer.parseInt( rrn.substring( 4, 6 ) );

		if ( day > 31 || ( day > 30 && ( month == 4 || month == 6 || month == 9 || month == 11 ) ) || ( day > 28 && month == 2 ) || rrn.length() != 14 ) {
			return false;
		}

		return validateCheckSum( rrn.toCharArray(), rrnValue.charAt( CHECK_DIGIT_INDEX ) );
	}

	private static boolean isValidFormat(String rrn) {
		return REGEX.matcher( rrn ).matches();
	}

	private boolean validateCheckSum(char[] rrnArray, char code) {
		int sum = 0;
		for ( int i = 0; i < CHECK_DIGIT_ARRAY.length; i++ ) {
			if ( i == 6 ) {
				continue;
			}
			sum += CHECK_DIGIT_ARRAY[i] * Character.getNumericValue( rrnArray[i] );
		}

		return ( 11 - sum % 11 ) % 10 == Character.getNumericValue( code );
	}
}
