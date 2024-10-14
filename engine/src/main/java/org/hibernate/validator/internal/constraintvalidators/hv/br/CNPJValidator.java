/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Eduardo Resende Batista Soares
 */
public class CNPJValidator implements ConstraintValidator<CNPJ, CharSequence> {
	private static final Pattern NUMBERS_UPPER_LETTERS_ONLY_REGEXP = Pattern.compile( "[0-9A-Z]+" );
	private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile( "([0-9A-Z]{2}[.]?[0-9A-Z]{3}[.]?[0-9A-Z]{3}[/]?[0-9A-Z]{4}[-]?[0-9]{2})" );
	private static final Pattern NUMERIC_PATTERN = Pattern.compile( "([0-9]{2}[.]?[0-9]{3}[.]?[0-9]{3}[/]?[0-9]{4}[-]?[0-9]{2})" );

	private Mod11CheckValidator withSeparatorMod11Validator1;
	private Mod11CheckValidator withSeparatorMod11Validator2;

	private Mod11CheckValidator withoutSeparatorMod11Validator1;
	private Mod11CheckValidator withoutSeparatorMod11Validator2;
	private Pattern pattern;

	@Override
	public void initialize(CNPJ constraintAnnotation) {
		if ( CNPJ.Format.NUMERIC.equals( constraintAnnotation.format() ) ) {
			this.withSeparatorMod11Validator1 = new Mod11CheckValidator();
			this.withSeparatorMod11Validator2 = new Mod11CheckValidator();
			this.withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
			this.withoutSeparatorMod11Validator2 = new Mod11CheckValidator();
			this.pattern = NUMERIC_PATTERN;
		}
		else {
			this.withSeparatorMod11Validator1 = new CnpjAlphanumericMod11CheckValidator();
			this.withSeparatorMod11Validator2 = new CnpjAlphanumericMod11CheckValidator();
			this.withoutSeparatorMod11Validator1 = new CnpjAlphanumericMod11CheckValidator();
			this.withoutSeparatorMod11Validator2 = new CnpjAlphanumericMod11CheckValidator();
			this.pattern = ALPHANUMERIC_PATTERN;
		}

		// validates CNPJ strings with separator, eg 91.509.901/0001-69
		// there are two checksums generated. The first over the digits prior the hyphen with the first
		// check digit being the digit directly after the hyphen. The second checksum is over all digits
		// pre hyphen + first check digit. The check digit in this case is the second digit after the hyphen
		withSeparatorMod11Validator1.initialize(
				0, 14, 16, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withSeparatorMod11Validator2.initialize(
				0, 16, 17, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);

		// validates CNPJ strings without separator, eg 91509901000169
		// checksums as described above, except there are no separator characters
		withoutSeparatorMod11Validator1.initialize(
				0, 11, 12, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
		withoutSeparatorMod11Validator2.initialize(
				0, 12, 13, true, 9, '0', '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
		);
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		if ( !pattern.matcher( value.toString() ).matches() ) {
			return false;
		}

		char firstDigit = value.charAt( 0 );
		char otherDigit = value.charAt( 1 );
		for ( int i = 2; i < value.length(); i++ ) {
			char c = value.charAt( i );
			if ( Character.isDigit( c ) && firstDigit != c ) {
				otherDigit = c;
			}
		}
		if ( firstDigit == otherDigit ) {
			return false;
		}

		if ( NUMBERS_UPPER_LETTERS_ONLY_REGEXP.matcher( value ).matches() ) {
			return withoutSeparatorMod11Validator1.isValid( value, context )
					&& withoutSeparatorMod11Validator2.isValid( value, context );
		}
		else {
			return withSeparatorMod11Validator1.isValid( value, context )
					&& withSeparatorMod11Validator2.isValid( value, context );
		}
	}


	private static class CnpjAlphanumericMod11CheckValidator extends Mod11CheckValidator {
		private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
		private static final Pattern NUMBERS_UPPER_LETTERS_ONLY_STRIP_REGEXP = Pattern.compile( "[^0-9A-Z]" );
		private static final int BASE_CHAR_INDEX = 48;

		@Override
		protected int extractDigit(char value) throws NumberFormatException {
			// In the CNPJ Check Digit (DV) calculation routine, the numeric and alphanumeric values will be replaced
			//   by the decimal value corresponding to the code in the ASCII table and the value 48 will be subtracted from it.
			//   In this way, the numeric characters will continue with the same amounts, and the alphanumeric characters will have
			//   the following values: A=17, B=18, C=19… and so on.
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
}
