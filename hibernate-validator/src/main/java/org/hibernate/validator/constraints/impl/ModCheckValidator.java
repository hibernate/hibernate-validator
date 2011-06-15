package org.hibernate.validator.constraints.impl;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ModCheck.ModType;

public class ModCheckValidator implements ConstraintValidator<ModCheck, String> {

	private static final String NUMBERS_ONLY_REGEXP = "[^0-9]";
	private static final int DEC_RADIX = 10;
	private int multiplier;
	private int rangeStart;
	private int rangeEnd;
	private int checkDigitIndex;
	private ModType modType;

	@Override
	public void initialize(ModCheck constraintAnnotation) {
		this.modType = constraintAnnotation.value();
		this.multiplier = constraintAnnotation.multiplier();
		this.rangeStart = constraintAnnotation.rangeStart();
		this.rangeEnd = constraintAnnotation.rangeEnd();
		this.checkDigitIndex = constraintAnnotation.checkDigitIndex();
		// TODO
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		boolean ret = true;
		if ( value != null ) {
			switch ( modType ) {
			case MOD10:
				ret = passesLuhnTest( value, multiplier );
				break;
			case MOD11:
				String input = value.replaceAll( NUMBERS_ONLY_REGEXP, "" );
				int modResult = mod11( input.substring( this.rangeStart, this.rangeEnd ), this.multiplier );
				ret = modResult == Character.digit( input.charAt( checkDigitIndex ), DEC_RADIX );
				break;
			}
		}
		return ret;
	}

	/**
	 * Mod10 (Luhn) algorithm implementation
	 * 
	 * @param value
	 * @param multiplicator
	 * @return
	 */
	private static boolean passesLuhnTest(final String value, final int multiplicator) {
		List<Integer> digits = extractNumbers( value );
		int sum = 0;
		boolean even = false;
		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			int digit = digits.get( index );
			if ( even ) {
				digit *= multiplicator;
			}
			if ( digit > 9 ) {
				digit = digit / 10 + digit % 10;
			}
			sum += digit;
			even = !even;
		}
		return sum % 10 == 0;
	}

	/**
	 * Calculate Modulus 11
	 * 
	 * @param value
	 *            extracts
	 * @param max_weight
	 *            maximum weight for multiplication
	 * @return the result of the mod11 function
	 */
	private static int mod11(final String value, final int max_weight) {
		int sum = 0;
		int weight = 2;

		List<Integer> digits = extractNumbers( value );
		for ( int index = digits.size() - 1; index >= 0; index-- ) {
			sum += digits.get( index ) * weight++;
			if ( weight > max_weight ) {
				weight = 2;
			}
		}
		int mod = 11 - ( sum % 11 );
		return ( mod > 9 ) ? 0 : mod;
	}

	/**
	 * Parses the {@link String} value as a {@link List} of {@link Integer} objects
	 * 
	 * @param value
	 *            the input string to be parsed
	 * @return List of Integer objects. Ignores non-numeric chars
	 */
	private static List<Integer> extractNumbers(final String value) {
		List<Integer> digits = new ArrayList<Integer>( value.length() );
		char[] chars = value.toCharArray();
		for ( char c : chars ) {
			if ( Character.isDigit( c ) ) {
				digits.add( Character.digit( c, 10 ) );
			}
		}
		return digits;
	}

}
