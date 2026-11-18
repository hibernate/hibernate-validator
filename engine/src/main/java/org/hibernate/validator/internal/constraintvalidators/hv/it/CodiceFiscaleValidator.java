package org.hibernate.validator.internal.constraintvalidators.hv.it;

import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.it.CodiceFiscale;

/**
 * Validator for {@link CodiceFiscale}.
 */
public class CodiceFiscaleValidator implements ConstraintValidator<CodiceFiscale, CharSequence> {

	/**
	 * The letters that replace the digits 0 to 9, in this order, when two people would otherwise share the same code ("omocodia").
	 */
	private static final String DIGIT_REPLACEMENTS = "LMNPQRSTUV";
	private static final String DIGIT = "[0-9" + DIGIT_REPLACEMENTS + "]";

	/**
	 * Surname, name, year, month, day (plus 40 for women), place of birth, check character.
	 */
	private static final Pattern NATURAL_PERSON_PATTERN = Pattern.compile(
			"[A-Z]{6}" + DIGIT + "{2}[ABCDEHLMPRST]" + DIGIT + "{2}[A-Z]" + DIGIT + "{3}[A-Z]",
			Pattern.CASE_INSENSITIVE
	);

	/**
	 * The value of a character in an odd position (counting from 1), indexed by digit or by letter of the alphabet.
	 */
	private static final int[] ODD_POSITION_VALUES = {
			1,
			0,
			5,
			7,
			9,
			13,
			15,
			17,
			19,
			21,
			2,
			4,
			18,
			20,
			11,
			3,
			6,
			8,
			12,
			14,
			16,
			10,
			22,
			25,
			24,
			23
	};

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		if ( value.length() == 11 ) {
			return PartitaIVAValidator.isValidNumber( value.toString() );
		}
		if ( !NATURAL_PERSON_PATTERN.matcher( value ).matches() ) {
			return false;
		}
		String code = value.toString().toUpperCase( Locale.ROOT );

		int day = digitAt( code, 9 ) * 10 + digitAt( code, 10 );
		if ( day > 40 ) {
			day -= 40;
		}
		if ( day < 1 || day > 31 ) {
			return false;
		}

		return code.charAt( 15 ) == calculateCheckCharacter( code );
	}

	private static int digitAt(String code, int index) {
		char character = code.charAt( index );
		return Character.isDigit( character ) ? Character.digit( character, 10 ) : DIGIT_REPLACEMENTS.indexOf( character );
	}

	private static char calculateCheckCharacter(String code) {
		int total = 0;
		for ( int i = 0; i < 15; i++ ) {
			char character = code.charAt( i );
			int index = Character.isDigit( character ) ? Character.digit( character, 10 ) : character - 'A';
			total += i % 2 == 0 ? ODD_POSITION_VALUES[index] : index;
		}
		return (char) ( 'A' + total % 26 );
	}
}
