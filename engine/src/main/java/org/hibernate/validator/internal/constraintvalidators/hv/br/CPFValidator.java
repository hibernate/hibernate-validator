/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

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

			int digit = DigitUtil.getAsciiDigit( character );
			if ( digit < 0 ) {
				return false;
			}
			allDigitsEqual &= character == firstDigit;
			if ( digitIndex < 9 ) {
				firstSum += digit * ( 10 - digitIndex );
				secondSum += digit * ( 11 - digitIndex );
			}
			else if ( digitIndex == 9 ) {
				if ( digit != ModUtil.toZeroMappedMod11CheckDigit( ModUtil.calculateMod11Check( firstSum ) ) ) {
					return false;
				}
				secondSum += digit * 2;
			}
			else if ( digit != ModUtil.toZeroMappedMod11CheckDigit( ModUtil.calculateMod11Check( secondSum ) ) ) {
				return false;
			}
			digitIndex++;
		}

		return !allDigitsEqual;
	}
}
