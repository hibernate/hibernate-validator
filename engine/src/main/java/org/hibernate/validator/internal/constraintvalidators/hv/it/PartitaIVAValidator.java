package org.hibernate.validator.internal.constraintvalidators.hv.it;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.it.PartitaIVA;

/**
 * Validator for {@link PartitaIVA}.
 */
public class PartitaIVAValidator implements ConstraintValidator<PartitaIVA, CharSequence> {

	private static final Pattern PATTERN = Pattern.compile( "\\d{11}" );
	private static final String ALL_ZEROS = "00000000000";

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

		// Luhn: the digits in even positions (counting from 1) are doubled, and 9 is subtracted when the result exceeds 9
		int total = 0;
		for ( int i = 0; i < number.length(); i++ ) {
			int digit = Character.digit( number.charAt( i ), 10 );
			if ( i % 2 == 1 ) {
				digit *= 2;
				if ( digit > 9 ) {
					digit -= 9;
				}
			}
			total += digit;
		}
		return total % 10 == 0;
	}
}
