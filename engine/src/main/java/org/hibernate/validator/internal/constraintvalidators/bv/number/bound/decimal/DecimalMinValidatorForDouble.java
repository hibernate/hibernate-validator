/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMinValidatorForDouble extends AbstractDecimalMinValidator<Double> {

	@Override
	protected int compare(Double number) {
		return DecimalNumberComparatorHelper.compare( number, minValue, InfinityNumberComparatorHelper.LESS_THAN );
	}
}
