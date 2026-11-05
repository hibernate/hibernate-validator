package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class MinValidatorForInteger extends AbstractMinValidator<Integer> {

	@Override
	protected int compare(Integer number) {
		return NumberComparatorHelper.compare( number.longValue(), minValue );
	}
}
