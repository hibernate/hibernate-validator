/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import java.math.BigDecimal;

/**
 * Check that the character sequence (e.g. string) being validated represents a number, and has a value
 * less than or equal to the maximum value specified.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class DecimalMaxValidatorForCharSequence extends AbstractDecimalMaxValidator<CharSequence> {

	@Override
	protected int compare(CharSequence number) {
		try {
			return DecimalNumberComparatorHelper.compare( new BigDecimal( number.toString() ), maxValue );
		}
		catch (NumberFormatException nfe) {
			return 1;
		}
	}
}
