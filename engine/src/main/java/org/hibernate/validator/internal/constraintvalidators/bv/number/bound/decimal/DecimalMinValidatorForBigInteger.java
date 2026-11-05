package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import java.math.BigInteger;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMinValidatorForBigInteger extends AbstractDecimalMinValidator<BigInteger> {

	@Override
	protected int compare(BigInteger number) {
		return DecimalNumberComparatorHelper.compare( number, minValue );
	}
}
