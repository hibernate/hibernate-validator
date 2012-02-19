/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.constraintvalidators;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ModCheck.ModType;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Mod check validator for MOD10 and MOD11 algorithms
 *
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 */
public class ModCheckValidator implements ConstraintValidator<ModCheck, CharSequence> {

	private static final String NUMBERS_ONLY_REGEXP = "[^0-9]";
	private static final int DEC_RADIX = 10;

	/**
	 * Multiplier used by the mod algorithms
	 */
	private int multiplier;

	/**
	 * The start index for the checksum calculation
	 */
	private int startIndex;

	/**
	 * The end index for the checksum calculation
	 */
	private int endIndex;

	/**
	 * The index of the checksum digit
	 */
	private int checkDigitIndex;

	/**
	 * The type of checksum algorithm
	 */
	private ModType modType;

	private boolean ignoreNonDigitCharacters;

	public void initialize(ModCheck constraintAnnotation) {
		this.modType = constraintAnnotation.modType();
		this.multiplier = constraintAnnotation.multiplier();
		this.startIndex = constraintAnnotation.startIndex();
		this.endIndex = constraintAnnotation.endIndex();
		this.checkDigitIndex = constraintAnnotation.checkDigitPosition();
		this.ignoreNonDigitCharacters = constraintAnnotation.ignoreNonDigitCharacters();

		if ( this.startIndex < 0 ) {
			throw new IllegalArgumentException(
					String.format( "Start index cannot be negative: %d", this.startIndex )
			);
		}

		if ( this.endIndex < 0 ) {
			throw new IllegalArgumentException(
					String.format( "End index cannot be negative: %d", this.startIndex )
			);
		}

		if ( this.startIndex > this.endIndex ) {
			throw new IllegalArgumentException(
					String.format( "Invalid Range: %d > %d", this.startIndex, this.endIndex )
			);
		}

		if ( checkDigitIndex > 0 && startIndex <= checkDigitIndex && endIndex > checkDigitIndex ) {
			throw new IllegalArgumentException(
					String.format(
							"A explicitly specified check digit must lie outside the interval: [%d, %d]",
							this.startIndex,
							this.endIndex
					)
			);
		}
	}

	public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String valueAsString = value.toString();
		if ( ignoreNonDigitCharacters ) {
			valueAsString = valueAsString.replaceAll( NUMBERS_ONLY_REGEXP, "" );
		}

		try {
			valueAsString = extractVerificationString( valueAsString );
		}
		catch ( IndexOutOfBoundsException e ) {
			return false;
		}

		List<Integer> digits;
		try {
			digits = extractDigits( valueAsString );
		}
		catch ( NumberFormatException e ) {
			return false;
		}

		boolean isValid;

		if ( modType.equals( ModType.MOD10 ) ) {
			isValid = ModUtil.passesMod10Test( digits, multiplier );
		}
		else {
			isValid = ModUtil.passesMod11Test( digits, multiplier );
		}
		return isValid;
	}

	private String extractVerificationString(String value) throws IndexOutOfBoundsException {
		// the whole string should be verified (check digit is implicit)
		if ( endIndex == Integer.MAX_VALUE ) {
			return value;
		}

		String verificationString = value.substring( startIndex, endIndex );

		// append the check digit of explicitly specified
		if ( checkDigitIndex > 0 ) {
			verificationString = verificationString + value.charAt( checkDigitIndex );
		}

		return verificationString;
	}

	/**
	 * Parses the {@link String} value as a {@link List} of {@link Integer} objects
	 *
	 * @param value the input string to be parsed
	 *
	 * @return List of {@code Integer} objects.
	 *
	 * @throws NumberFormatException in case ant of the characters is not a digit
	 */
	private List<Integer> extractDigits(final String value) throws NumberFormatException {
		List<Integer> digits = new ArrayList<Integer>( value.length() );
		char[] chars = value.toCharArray();
		for ( char c : chars ) {
			if ( Character.isDigit( c ) ) {
				digits.add( Character.digit( c, DEC_RADIX ) );
			}
			else {
				throw new NumberFormatException( String.format( "'%s' is not a digit", c ) );
			}
		}
		return digits;
	}
}
