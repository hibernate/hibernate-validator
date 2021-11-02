/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.internal.constraintvalidators.hv.ModCheckBase;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Validator for {@link PESEL}.
 *
 * @author Marko Bekhta
 */
public class PESELValidator extends ModCheckBase implements ConstraintValidator<PESEL, CharSequence> {

	private static final int[] WEIGHTS_PESEL = { 1, 3, 7, 9, 1, 3, 7, 9, 1, 3 };

	@Override
	public void initialize(PESEL constraintAnnotation) {
		super.initialize(
				0,
				Integer.MAX_VALUE,
				-1,
				false
		);
	}

	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		// if the length of the number is incorrect we can return fast
		if ( digits.size() != WEIGHTS_PESEL.length ) {
			return false;
		}

		int monthCode = doubleDigitNumberFromSubList( digits, 2 );
		try {
			// PESEL format is YYMMDD*****, where MM is coded month (depending on the century
			// 0/20/40/60/80 can be added to the month value) see javadoc on `year()`.
			// Need to make sure that these first 6 digits represent a valid date
			LocalDate.of(
					year( doubleDigitNumberFromSubList( digits, 0 ), monthCode / 20 ),
					monthCode % 20,
					doubleDigitNumberFromSubList( digits, 4 )
			);
		}
		catch (DateTimeException e) {
			return false;
		}

		// now that we are done with custom logic we can proceeed with regular mod check of the checkdigit:
		Collections.reverse( digits );

		int modResult = ModUtil.calculateModXCheckWithWeights( digits, 10, Integer.MAX_VALUE, WEIGHTS_PESEL );
		switch ( modResult ) {
			case 10:
				return checkDigit == '0';
			default:
				return Character.isDigit( checkDigit ) && modResult == extractDigit( checkDigit );
		}
	}

	private int doubleDigitNumberFromSubList(List<Integer> digits, int start) {
		// index access is ok here as we use ArrayLists.
		return digits.get( start ) * 10 + digits.get( start + 1 );
	}

	/**
	 * 1800–1899 - 80
	 * 1900–1999 - 00
	 * 2000–2099 - 20
	 * 2100–2199 - 40
	 * 2200–2299 - 60
	 */
	private int year(int year, int centuryCode) {
		switch ( centuryCode ) {
			case 4: return 1800 + year;
			case 0: return 1900 + year;
			case 1: return 2000 + year;
			case 2: return 2100 + year;
			case 3: return 2200 + year;
			default:
				throw new IllegalStateException( "Invalid century code." );
		}
	}

}
