/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

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
		int checkDigit = DigitUtil.getDecimalDigit( value.charAt( length - 1 ) );
		if ( checkDigit < 0 ) {
			return false;
		}
		long sum = ModUtil.calculateLuhnSum( value, 0, length - 1, ignoreNonDigitCharacters );
		return sum >= 0 && checkDigit == ( 10 - sum % 10 ) % 10;
	}
}
