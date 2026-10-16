/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Luhn algorithm checksum validator
 *
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class LuhnCheckValidator extends ModCheckBase
		implements ConstraintValidator<LuhnCheck, CharSequence> {
	@Override
	public void initialize(LuhnCheck constraintAnnotation) {
		super.initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitIndex(),
				constraintAnnotation.ignoreNonDigitCharacters()
		);
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		int checkDigitIndex = getCheckDigitIndex( length );
		if ( checkDigitIndex < 0 || checkDigitIndex >= length ) {
			return false;
		}
		int checkDigit = DigitUtil.getDecimalDigit( value.charAt( checkDigitIndex ) );
		if ( checkDigit < 0 ) {
			return false;
		}
		long sum = ModUtil.calculateLuhnSum( value, getVerificationStartIndex(), getVerificationEndIndex( length ), ignoreDelimitingCharacters );
		if ( sum < 0 ) {
			return false;
		}
		// Preserve the original integer accumulator's overflow before calculating the check digit.
		int legacySum = (int) sum;
		return checkDigit == ( 10 - legacySum % 10 ) % 10;
	}

	/**
	 * Validate check digit using Luhn algorithm
	 *
	 * @param digits The digits over which to calculate the checksum
	 * @param checkDigit the check digit
	 *
	 * @return {@code true} if the luhn check result matches the check digit, {@code false} otherwise
	 */
	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		int modResult = ModUtil.calculateLuhnMod10Check( digits );

		if ( !Character.isDigit( checkDigit ) ) {
			return false;
		}

		int checkValue = extractDigit( checkDigit );
		return checkValue == modResult;
	}
}
