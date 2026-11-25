package org.hibernate.validator.internal.constraintvalidators.hv.it;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.it.PartitaIVA;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Validator for {@link PartitaIVA}.
 * <p>
 * The last of the 11 digits is a check digit, computed as described by article 9 of the Italian Ministerial Decree
 * of 23 December 1976 ("Sistemi di codificazione dei soggetti da iscrivere all'anagrafe tributaria"): the procedure
 * is equivalent to the Luhn algorithm, with positions counted from the left. As the number of digits is odd,
 * counting from the right, as Luhn is usually described, doubles the same digits.
 *
 * @see <a href="https://it.wikipedia.org/wiki/Partita_IVA#Calcolo_e_verifica_della_cifra_di_controllo">Partita IVA: calcolo e verifica della cifra di controllo (Wikipedia)</a>
 * @see <a href="https://en.wikipedia.org/wiki/Luhn_algorithm">Luhn algorithm (Wikipedia)</a>
 */
public class PartitaIVAValidator implements ConstraintValidator<PartitaIVA, CharSequence> {

	private static final Pattern PATTERN = Pattern.compile( "\\d{11}" );
	private static final String ALL_ZEROS = "00000000000";
	private static final int CHECK_DIGIT_INDEX = 10;

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		return value == null || isValidNumber( value.toString() );
	}

	/**
	 * The 11-digit tax code of a legal entity follows the same rules, hence the sharing with {@link CodiceFiscaleValidator}.
	 */
	static boolean isValidNumber(String number) {
		if ( !PATTERN.matcher( number ).matches() || ALL_ZEROS.equals( number ) ) {
			return false;
		}

		// the Luhn sum of the first ten digits doubles the rightmost of them, that is the digits in even positions (counting from 1)
		long sum = ModUtil.calculateLuhnSum( number, 0, CHECK_DIGIT_INDEX, false );
		return DigitUtil.getAsciiDigit( number.charAt( CHECK_DIGIT_INDEX ) ) == ( 10 - sum % 10 ) % 10;
	}
}
