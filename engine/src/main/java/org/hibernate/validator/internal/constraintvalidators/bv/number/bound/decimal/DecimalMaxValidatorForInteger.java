package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMaxValidatorForInteger extends AbstractDecimalMaxValidator<Integer> {

	@Override
	protected int compare(Integer number) {
		return DecimalNumberComparatorHelper.compare( number.longValue(), maxValue );
	}
}
