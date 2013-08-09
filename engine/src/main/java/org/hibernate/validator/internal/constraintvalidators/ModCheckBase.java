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
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * ModCheckBase contains all shared methods and options used by Mod Check Validators
 *
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */

public abstract class ModCheckBase {

	protected static final String NUMBERS_ONLY_REGEXP = "[^0-9]";
	protected static final int DEC_RADIX = 10;
	private static final Log log = LoggerFactory.make();
	/**
	 * Multiplier used by the mod algorithms
	 */
	protected int multiplier;
	/**
	 * The start index for the checksum calculation
	 */
	protected int startIndex;
	/**
	 * The end index for the checksum calculation
	 */
	protected int endIndex;
	/**
	 * The index of the checksum digit
	 */
	protected int checkDigitIndex;
	protected boolean ignoreNonDigitCharacters;

	/**
	 * Parses the {@link String} value as a {@link List} of {@link Integer} objects
	 *
	 * @param value the input string to be parsed
	 *
	 * @return List of {@code Integer} objects.
	 *
	 * @throws NumberFormatException in case any of the characters is not a digit
	 */
	protected static List<Integer> extractDigits(final String value) throws NumberFormatException {
		List<Integer> digits = new ArrayList<Integer>( value.length() );
		char[] chars = value.toCharArray();
		for ( char c : chars ) {
			digits.add( extractDigit( c ) );
		}
		return digits;
	}

	/**
	 * Returns the numeric {@code int} value of a {@code char}
	 *
	 * @param value the input {@code char} to be parsed
	 *
	 * @return the numeric {@code int} value represented by the character.
	 *
	 * @throws NumberFormatException in case character is not a digit
	 */
	protected static int extractDigit(char value) throws NumberFormatException {
		if ( Character.isDigit( value ) ) {
			return Character.digit( value, DEC_RADIX );
		}
		else {
			throw log.getCharacterIsNotADigitException( value );
		}
	}

	public abstract boolean isCheckDigitValid(List<Integer> digits, char checkDigit);

	public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String valueAsString = this.checkString( value.toString() );

		String digitsAsString;
		char checkDigit;
		try {
			digitsAsString = this.extractVerificationString( valueAsString );
			checkDigit = this.extractCheckDigit( valueAsString );
		}
		catch ( IndexOutOfBoundsException e ) {
			return false;
		}

		List<Integer> digits;

		try {
			digits = extractDigits( digitsAsString );
		}
		catch ( NumberFormatException e ) {
			return false;
		}

		return this.isCheckDigitValid( digits, checkDigit );
	}

	public boolean validateOptions() {

		if ( this.startIndex < 0 ) {
			throw log.getStartIndexCannotBeNegativeException( this.startIndex );
		}

		if ( this.endIndex < 0 ) {
			throw log.getEndIndexCannotBeNegativeException( this.endIndex );
		}

		if ( this.startIndex > this.endIndex ) {
			throw log.getInvalidRangeException( this.startIndex, this.endIndex );
		}

		if ( this.checkDigitIndex > 0 && this.startIndex <= this.checkDigitIndex && this.endIndex > this.checkDigitIndex ) {
			throw log.getInvalidCheckDigitException( this.startIndex, this.endIndex );
		}

		if ( this.multiplier < 0 ) {
			throw log.getMultiplierCannotBeNegativeException( this.multiplier );
		}

		return true;
	}

	protected String checkString(String value) {
		if ( ignoreNonDigitCharacters ) {
			return value.replaceAll( NUMBERS_ONLY_REGEXP, "" );
		}
		else {
			return value;
		}
	}

	protected String extractVerificationString(String value) throws IndexOutOfBoundsException {
		// the string contains the check digit, just return the digits to verify
		String verification = this.checkString( value );
		if ( endIndex == Integer.MAX_VALUE ) {
			return verification.substring( 0, value.length() - 1 );
		}
		else {
			return verification.substring( startIndex, endIndex );
		}
	}

	protected char extractCheckDigit(String value) throws IndexOutOfBoundsException {
		// the string contains the check digit, just return the check digit
		String verification = this.checkString( value );
		if ( checkDigitIndex == -1 ) {
			return verification.charAt( value.length() - 1 );
		}
		else {
			return verification.charAt( checkDigitIndex );
		}
	}

}
