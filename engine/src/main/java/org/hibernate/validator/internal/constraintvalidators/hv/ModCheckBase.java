/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidatorContext;

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

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Pattern NUMBERS_ONLY_REGEXP = Pattern.compile( "[^0-9]" );

	private static final int DEC_RADIX = 10;

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

	protected boolean ignoreDelimitingCharacters;

	public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String valueAsString = value.toString();
		String digitsAsString;
		char checkDigit;
		try {
			digitsAsString = extractVerificationString( valueAsString );
			checkDigit = extractCheckDigit( valueAsString );
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
		digitsAsString = stripNonDigitsIfRequired( digitsAsString );

		List<Integer> digits;
		try {
			digits = extractDigits( digitsAsString );
		}
		catch (NumberFormatException e) {
			return false;
		}

		return this.isCheckDigitValid( digits, checkDigit );
	}

	public abstract boolean isCheckDigitValid(List<Integer> digits, char checkDigit);

	protected void initialize(int startIndex, int endIndex, int checkDigitIndex, boolean ignoreDelimitingCharacters) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.checkDigitIndex = checkDigitIndex;
		this.ignoreDelimitingCharacters = ignoreDelimitingCharacters;

		this.validateOptions();
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
	protected int extractDigit(char value) throws NumberFormatException {
		if ( Character.isDigit( value ) ) {
			return Character.digit( value, DEC_RADIX );
		}
		else {
			throw LOG.getCharacterIsNotADigitException( value );
		}
	}

	/**
	 * Parses the {@link String} value as a {@link List} of {@link Integer} objects
	 *
	 * @param value the input string to be parsed
	 *
	 * @return List of {@code Integer} objects.
	 *
	 * @throws NumberFormatException in case any of the characters is not a digit
	 */
	private List<Integer> extractDigits(final String value) throws NumberFormatException {
		List<Integer> digits = new ArrayList<Integer>( value.length() );
		char[] chars = value.toCharArray();
		for ( char c : chars ) {
			digits.add( extractDigit( c ) );
		}
		return digits;
	}

	private boolean validateOptions() {
		if ( this.startIndex < 0 ) {
			throw LOG.getStartIndexCannotBeNegativeException( this.startIndex );
		}

		if ( this.endIndex < 0 ) {
			throw LOG.getEndIndexCannotBeNegativeException( this.endIndex );
		}

		if ( this.startIndex > this.endIndex ) {
			throw LOG.getInvalidRangeException( this.startIndex, this.endIndex );
		}

		if ( this.checkDigitIndex > 0 && this.startIndex <= this.checkDigitIndex && this.endIndex > this.checkDigitIndex ) {
			throw LOG.getInvalidCheckDigitException( this.startIndex, this.endIndex );
		}

		return true;
	}

	protected String stripNonDigitsIfRequired(String value) {
		if ( ignoreDelimitingCharacters ) {
			return NUMBERS_ONLY_REGEXP.matcher( value ).replaceAll( "" );
		}
		else {
			return value;
		}
	}

	private String extractVerificationString(String value) throws IndexOutOfBoundsException {
		return value.substring( getVerificationStartIndex(), getVerificationEndIndex( value.length() ) );
	}

	private char extractCheckDigit(String value) throws IndexOutOfBoundsException {
		return value.charAt( getCheckDigitIndex( value.length() ) );
	}

	protected int getVerificationStartIndex() {
		// The default end index historically ignores startIndex and uses the whole body.
		return endIndex == Integer.MAX_VALUE ? 0 : startIndex;
	}

	protected int getVerificationEndIndex(int length) {
		if ( endIndex == Integer.MAX_VALUE ) {
			return length - 1;
		}
		return checkDigitIndex == -1 || checkDigitIndex == endIndex ? endIndex : endIndex + 1;
	}

	protected int getCheckDigitIndex(int length) {
		if ( checkDigitIndex == -1 ) {
			return endIndex == Integer.MAX_VALUE ? length - 1 : endIndex;
		}
		return checkDigitIndex;
	}

}
