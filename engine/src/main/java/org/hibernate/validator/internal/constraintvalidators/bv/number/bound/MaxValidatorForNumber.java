/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class MaxValidatorForNumber extends AbstractMaxValidator<Number> {

	@Override
	protected int compare(Number number) {
		return NumberComparatorHelper.compare( number, maxValue, InfinityNumberComparatorHelper.GREATER_THAN );
	}
}
