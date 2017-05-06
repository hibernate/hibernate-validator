/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMinValidatorForDouble extends BaseDecimalMinValidator<Double> {

	@Override protected int compare(Double number) {
		return DecimalNumberComparatorHelper.compare( number, minValue, -1 );
	}
}
