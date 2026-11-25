package org.hibernate.validator.internal.constraintvalidators.hv.it;

import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.it.CodiceFiscale;
import org.hibernate.validator.internal.util.DigitUtil;

/**
 * Validator for {@link CodiceFiscale}.
 * <p>
 * The structure of the code, the letters of the months, the letters replacing the digits and the computation
 * of the check character are defined by the Italian Ministerial Decree of 23 December 1976
 * ("Sistemi di codificazione dei soggetti da iscrivere all'anagrafe tributaria"), published in the ordinary supplement
 * to the Gazzetta Ufficiale no. 345 of 29 December 1976.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Italian_fiscal_code#Fiscal_code_generation">Italian fiscal code (Wikipedia)</a>
 * @see <a href="https://it.wikipedia.org/wiki/Codice_fiscale#Per_le_persone_fisiche">Codice fiscale (Wikipedia)</a>
 */
public class CodiceFiscaleValidator implements ConstraintValidator<CodiceFiscale, CharSequence> {

	/**
	 * The letters that replace the digits 0 to 9, in this order, when two people would otherwise share the same code ("omocodia").
	 */
	private static final String DIGIT_REPLACEMENTS = "LMNPQRSTUV";
	private static final String DIGIT = "[0-9" + DIGIT_REPLACEMENTS + "]";

	/**
	 * The letters of the months, from January to December, and the number of days of each.
	 */
	private static final String MONTHS = "ABCDEHLMPRST";
	private static final int[] DAYS_IN_MONTH = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int FEBRUARY = 1;

	/**
	 * Surname, name, year, month, day (plus 40 for women), place of birth, check character.
	 */
	private static final Pattern NATURAL_PERSON_PATTERN = Pattern.compile(
			"[A-Z]{6}" + DIGIT + "{2}[" + MONTHS + "]" + DIGIT + "{2}[A-Z]" + DIGIT + "{3}[A-Z]",
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

		if ( !hasValidDateOfBirth( code ) ) {
			return false;
		}

		return code.charAt( 15 ) == calculateCheckCharacter( code );
	}

	private static boolean hasValidDateOfBirth(String code) {
		int year = digitAt( code, 6 ) * 10 + digitAt( code, 7 );
		int month = MONTHS.indexOf( code.charAt( 8 ) );
		int day = digitAt( code, 9 ) * 10 + digitAt( code, 10 );
		if ( day > 40 ) {
			day -= 40;
		}
		if ( day < 1 || day > DAYS_IN_MONTH[month] ) {
			return false;
		}
		// the century is not part of the code: a 29th of February is accepted whenever the year is a leap year in at least one century
		return month != FEBRUARY || day < 29 || year % 4 == 0;
	}

	private static int digitAt(String code, int index) {
		char character = code.charAt( index );
		int digit = DigitUtil.getAsciiDigit( character );
		return digit >= 0 ? digit : DIGIT_REPLACEMENTS.indexOf( character );
	}

	private static char calculateCheckCharacter(String code) {
		int total = 0;
		for ( int i = 0; i < 15; i++ ) {
			char character = code.charAt( i );
			int digit = DigitUtil.getAsciiDigit( character );
			int index = digit >= 0 ? digit : character - 'A';
			total += i % 2 == 0 ? ODD_POSITION_VALUES[index] : index;
		}
		return (char) ( 'A' + total % 26 );
	}
}
