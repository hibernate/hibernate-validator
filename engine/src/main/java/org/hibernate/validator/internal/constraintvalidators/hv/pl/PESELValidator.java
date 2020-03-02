/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

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
		Collections.reverse( digits );

		// if the length of the number is incorrect we can return fast
		if ( digits.size() != WEIGHTS_PESEL.length  ) {
			return false;
		}

		int modResult = ModUtil.calculateModXCheckWithWeights( digits, 10, Integer.MAX_VALUE, WEIGHTS_PESEL );
		switch ( modResult ) {
			case 10:
				return checkDigit == '0';
			default:
				return Character.isDigit( checkDigit ) && modResult == extractDigit( checkDigit );
		}
	}
}
