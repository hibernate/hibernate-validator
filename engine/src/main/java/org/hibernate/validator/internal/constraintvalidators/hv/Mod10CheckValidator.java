/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Mod10 (Luhn algorithm implementation) Check validator
 *
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class Mod10CheckValidator extends ModCheckBase
		implements ConstraintValidator<Mod10Check, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Multiplier to be used by odd digits on Mod10 algorithm
	 */
	private int multiplier;

	/**
	 * Weight to be used by even digits on Mod10 algorithm
	 */
	private int weight;

	@Override
	public void initialize(Mod10Check constraintAnnotation) {
		super.initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitIndex(),
				constraintAnnotation.ignoreNonDigitCharacters()
		);
		this.multiplier = constraintAnnotation.multiplier();
		this.weight = constraintAnnotation.weight();

		if ( this.multiplier < 0 ) {
			throw LOG.getMultiplierCannotBeNegativeException( this.multiplier );
		}
		if ( this.weight < 0 ) {
			throw LOG.getWeightCannotBeNegativeException( this.weight );
		}
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
		return checkDigit >= 0
				&& checkDigit == ModUtil.calculateMod10Check(
						value, getVerificationStartIndex(), getVerificationEndIndex( length ), ignoreDelimitingCharacters, multiplier, weight
				);
	}

	/**
	 * Validate check digit using Mod10
	 *
	 * @param digits The digits over which to calculate the checksum
	 * @param checkDigit the check digit
	 *
	 * @return {@code true} if the mod 10 result matches the check digit, {@code false} otherwise
	 */
	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		int modResult = ModUtil.calculateMod10Check( digits, this.multiplier, this.weight );

		if ( !Character.isDigit( checkDigit ) ) {
			return false;
		}

		int checkValue = extractDigit( checkDigit );
		return checkValue == modResult;
	}
}
