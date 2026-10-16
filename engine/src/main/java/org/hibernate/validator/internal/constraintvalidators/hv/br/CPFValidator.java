/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.CPF;

/**
 * @author Hardy Ferentschik
 */
public class CPFValidator implements ConstraintValidator<CPF, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		int length = value.length();
		if ( length != 11 && length != 14 ) {
			return false;
		}

		// The two check digits use weights 10..2 and 11..2, respectively.
		int firstSum = 0;
		int secondSum = 0;
		int digitIndex = 0;
		char firstDigit = value.charAt( 0 );
		boolean allDigitsEqual = true;
		for ( int i = 0; i < length; i++ ) {
			char character = value.charAt( i );
			if ( length == 14 ) {
				if ( i == 3 || i == 7 ) {
					if ( character != '.' ) {
						return false;
					}
					continue;
				}
				if ( i == 11 ) {
					if ( character != '-' ) {
						return false;
					}
					continue;
				}
			}

			if ( character < '0' || character > '9' ) {
				return false;
			}
			allDigitsEqual &= character == firstDigit;
			int digit = character - '0';
			if ( digitIndex < 9 ) {
				firstSum += digit * ( 10 - digitIndex );
				secondSum += digit * ( 11 - digitIndex );
			}
			else if ( digitIndex == 9 ) {
				if ( digit != checkDigit( firstSum ) ) {
					return false;
				}
				secondSum += digit * 2;
			}
			else if ( digit != checkDigit( secondSum ) ) {
				return false;
			}
			digitIndex++;
		}

		return !allDigitsEqual;
	}

	private static int checkDigit(int sum) {
		// Remainders zero and one both map to a zero check digit.
		int remainder = sum % 11;
		return remainder < 2 ? 0 : 11 - remainder;
	}
}
