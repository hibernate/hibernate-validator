/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.TituloEleitoral;

/**
 * Validates Brazilian voter registration numbers.
 */
public class TituloEleitoralValidator implements ConstraintValidator<TituloEleitoral, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		if ( value.length() != 12 ) {
			return false;
		}

		int firstSum = 0;
		int secondSum = 0;
		for ( int i = 0; i < 12; i++ ) {
			char character = value.charAt( i );
			if ( character < '0' || character > '9' ) {
				return false;
			}
			int digit = character - '0';
			if ( i < 8 ) {
				// The first check digit covers positions zero through seven with weights nine through two.
				firstSum += digit * ( 9 - i );
			}
			else if ( i < 11 ) {
				// The second includes positions eight and nine and the first check digit, with weights four, three, two.
				secondSum += digit * ( 12 - i );
			}
		}

		return value.charAt( 10 ) - '0' == checkDigit( firstSum )
				&& value.charAt( 11 ) - '0' == checkDigit( secondSum );
	}

	private static int checkDigit(int sum) {
		int remainder = sum % 11;
		return remainder < 2 ? 0 : 11 - remainder;
	}
}
