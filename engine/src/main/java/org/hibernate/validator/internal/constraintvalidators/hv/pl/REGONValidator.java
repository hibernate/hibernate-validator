/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.pl.REGON;

/**
 * Validator for {@link REGON}. Validates both 9 and 14 digits REGON numbers.
 *
 * @author Michal Domagala
 */
public class REGONValidator implements ConstraintValidator<REGON, CharSequence> {

	// last weight is zero to ignore check digit when sum and modulo is calculated
	private static final int[] WEIGHTS_REGON_9 = {8, 9, 2, 3, 4, 5, 6, 7, 0};

	private static final int[] WEIGHTS_REGON_14 = {2, 4, 8, 5, 0, 9, 7, 3, 6, 1, 2, 4, 8, 0};

	@Override
	public void initialize(REGON constraintAnnotation) {
	}

	@Override
	public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		int[] digits = value.chars()
			.map( Character::getNumericValue )
			.toArray();

		for ( int digit : digits ) {
			if ( digit < 0 || digit > 9 ) {
				return false; // non-digit in input string
			}
		}

		switch ( digits.length ) {
			case 9:
				int checkDigit9 = digits[8];
				return mod11mod10( digits, WEIGHTS_REGON_9 ) == checkDigit9;
			case 14:
				int checkDigit14 = digits[13];
				return mod11mod10( digits, WEIGHTS_REGON_14 ) == checkDigit14;
			default:
				return false;
		}
	}

	// check digit has weight 0 -> check digit is effectively ignored by sum and modulo
	private int mod11mod10(int[] digits, int[] weigths) {
		int sum = 0;
		for ( int i = 0; i < digits.length; i++ ) {
			sum += digits[i] * weigths[i];
		}
		// sum modulo 11 could be 10. 10 is treat as zero
		return ( sum % 11 ) % 10;
	}
}
