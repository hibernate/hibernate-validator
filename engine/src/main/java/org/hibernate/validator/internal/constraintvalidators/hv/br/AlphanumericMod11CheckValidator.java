/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Modulo 11 validator for digits and uppercase ASCII letters using
 * the character's ASCII value minus 48 as its numeric value.
 *
 * @author Matheus Pereira
 */
class AlphanumericMod11CheckValidator extends Mod11CheckValidator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
	private static final Pattern NUMBERS_UPPER_LETTERS_ONLY_STRIP_REGEXP = Pattern.compile( "[^0-9A-Z]" );
	private static final int BASE_CHAR_INDEX = 48;

	@Override
	protected int extractDigit(char value) throws NumberFormatException {
		if ( Character.isDigit( value ) || ( value >= 'A' && value <= 'Z' ) ) {
			return value - BASE_CHAR_INDEX;
		}
		else {
			throw LOG.getCharacterIsNotDigitOrUpperCaseLetterException( value );
		}
	}

	@Override
	protected String stripNonDigitsIfRequired(String value) {
		if ( ignoreDelimitingCharacters ) {
			return NUMBERS_UPPER_LETTERS_ONLY_STRIP_REGEXP.matcher( value ).replaceAll( "" );
		}
		else {
			return value;
		}
	}
}
