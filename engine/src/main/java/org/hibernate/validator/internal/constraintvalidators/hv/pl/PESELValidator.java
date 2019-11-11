/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.pl.PESEL;

import static java.lang.Character.getNumericValue;
import static java.lang.Integer.parseInt;

/**
 * Validator for {@link PESEL}.
 *
 * @author Marko Bekhta
 */
public class PESELValidator implements ConstraintValidator<PESEL, CharSequence> {

	private static final int PESEL_LENGTH = 11;

	private static final int[] WEIGHTS_PESEL = { 1, 3, 7, 9, 1, 3, 7, 9, 1, 3, 1 };
	private static final Pattern VALID_PESEL_REGEX = Pattern.compile("[0-9]{11}");
	private static final Map<Integer, Integer> YEAR_CODES = new HashMap<>();

	static {
		YEAR_CODES.put(0, 1900);
		YEAR_CODES.put(20, 2000);
		YEAR_CODES.put(40, 2100);
		YEAR_CODES.put(60, 2200);
		YEAR_CODES.put(80, 1800);
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		final String pesel = value.toString();

		return containsValidCharacters(pesel)
				&& isCheckSumValid(pesel)
				&& isDateValid(pesel);

	}

	private boolean isDateValid(String pesel) {
		int year = parseInt(pesel.substring(0, 2));
		int monthWithCenturyCode = parseInt(pesel.substring(2, 4));
		int month = monthWithCenturyCode % 20;
		int dayOfMonth = parseInt(pesel.substring(4, 6));

		year = extractCenturyFirstYear(month, monthWithCenturyCode) + year;

		try {
			LocalDate.of(year, month, dayOfMonth);
		} catch (DateTimeException e) {
			return false;
		}
		return true;
	}

	private int extractCenturyFirstYear(int month, int monthWithYearCode) {
		final int yearCode = monthWithYearCode - month;
		return YEAR_CODES.get(yearCode);
	}

	private boolean containsValidCharacters(String pesel) {
		return VALID_PESEL_REGEX.matcher(pesel).matches();
	}

	private boolean isCheckSumValid(String pesel) {
		final int[] digitsOfPesel = peselToDigitsArray(pesel);
		return (calculateWeightedSum(digitsOfPesel) % 10) == 0;
	}

	private int[] peselToDigitsArray(String pesel) {
		int[] digits = new int[PESEL_LENGTH];
		final char[] peselCharacters = pesel.toCharArray();

		for (int i = 0; i < PESEL_LENGTH; i++) {
			digits[i] = getNumericValue(peselCharacters[i]);
		}
		return digits;
	}

	private int calculateWeightedSum(int[] digitsOfPesel) {
		return (digitsOfPesel[0] * WEIGHTS_PESEL[0])
				+ (digitsOfPesel[1] * WEIGHTS_PESEL[1])
				+ (digitsOfPesel[2] * WEIGHTS_PESEL[2])
				+ (digitsOfPesel[3] * WEIGHTS_PESEL[3])
				+ (digitsOfPesel[4] * WEIGHTS_PESEL[4])
				+ (digitsOfPesel[5] * WEIGHTS_PESEL[5])
				+ (digitsOfPesel[6] * WEIGHTS_PESEL[6])
				+ (digitsOfPesel[7] * WEIGHTS_PESEL[7])
				+ (digitsOfPesel[8] * WEIGHTS_PESEL[8])
				+ (digitsOfPesel[9] * WEIGHTS_PESEL[9])
				+ (digitsOfPesel[10] * WEIGHTS_PESEL[10]);
	}

}
