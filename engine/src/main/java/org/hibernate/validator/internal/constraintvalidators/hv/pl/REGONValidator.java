/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.internal.constraintvalidators.hv.ModCheckBase;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * A class for REGON validator. Validates both 9 and 14 digits REGON numbers.
 *
 * @author Marko Bekhta
 */
public class REGONValidator extends ModCheckBase implements ConstraintValidator<REGON, CharSequence> {

	private static final int[] WEIGHTS_REGON_14 = { 2, 4, 8, 5, 0, 9, 7, 3, 6, 1, 2, 4, 8 };

	private static final int[] WEIGHTS_REGON_9 = { 8, 9, 2, 3, 4, 5, 6, 7 };

	@Override
	public void initialize(REGON constraintAnnotation) {
		super.initialize(
				0,
				Integer.MAX_VALUE,
				-1,
				false
		);
	}

	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		Collections.reverse( digits );

		// as we need sum % 11 rather than 11 - (sum % 11) returned by Mod11 algorithm:
		int modResult = 11 - ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE, getWeights( digits ) );
		switch ( modResult ) {
			case 10:
			case 11:
				return checkDigit == 0;
			default:
				return Character.isDigit( checkDigit ) && modResult == extractDigit( checkDigit );
		}
	}

	/**
	 * @param digits a list of digits to be verified. They are used to determine a size of REGON number - is it 9 or 14 digit number
	 *
	 * @return an array of weights to be used to calculate a checksum
	 */
	private int[] getWeights(List<Integer> digits) {
		if ( digits.size() == 8 ) {
			return WEIGHTS_REGON_9;
		}
		else if ( digits.size() == 13 ) {
			return WEIGHTS_REGON_14;
		}
		else {
			return new int[] { };
		}
	}

	;
}
