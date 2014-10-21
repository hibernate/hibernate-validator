/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.List;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Luhn algorithm checksum validator
 *
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class LuhnCheckValidator extends ModCheckBase
		implements ConstraintValidator<LuhnCheck, CharSequence> {
	@Override
	public void initialize(LuhnCheck constraintAnnotation) {
		super.initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitIndex(),
				constraintAnnotation.ignoreNonDigitCharacters()
		);
	}

	/**
	 * Validate check digit using Luhn algorithm
	 *
	 * @param digits The digits over which to calculate the checksum
	 * @param checkDigit the check digit
	 *
	 * @return {@code true} if the luhn check result matches the check digit, {@code false} otherwise
	 */
	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		int modResult = ModUtil.calculateLuhnMod10Check( digits );

		if ( !Character.isDigit( checkDigit ) ) {
			return false;
		}

		int checkValue = extractDigit( checkDigit );
		return checkValue == modResult;
	}
}
