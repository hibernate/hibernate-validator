/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.CreditCardNumber;

/**
 * Validates credit card numbers using the Luhn checksum.
 */
public class CreditCardNumberValidator implements ConstraintValidator<CreditCardNumber, CharSequence> {

	private boolean ignoreNonDigitCharacters;

	@Override
	public void initialize(CreditCardNumber constraintAnnotation) {
		ignoreNonDigitCharacters = constraintAnnotation.ignoreNonDigitCharacters();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		int length = value.length();
		if ( length == 0 ) {
			return false;
		}

		// The check digit is always the last original character, including when separators are ignored.
		char checkCharacter = value.charAt( length - 1 );
		if ( !Character.isDigit( checkCharacter ) ) {
			return false;
		}

		int sum = 0;
		boolean doubleDigit = true;
		for ( int i = length - 2; i >= 0; i-- ) {
			char character = value.charAt( i );
			if ( ignoreNonDigitCharacters ) {
				// Preserve the ASCII-only normalization used by the composing Luhn constraint.
				if ( character < '0' || character > '9' ) {
					continue;
				}
			}
			else if ( !Character.isDigit( character ) ) {
				return false;
			}

			int digit = Character.digit( character, 10 );
			if ( doubleDigit ) {
				digit *= 2;
				if ( digit > 9 ) {
					digit -= 9;
				}
			}
			// Only the remainder is needed; keep the accumulator bounded for arbitrarily long inputs.
			sum = ( sum + digit ) % 10;
			doubleDigit = !doubleDigit;
		}

		return Character.digit( checkCharacter, 10 ) == ( 10 - sum ) % 10;
	}
}
