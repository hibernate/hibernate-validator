/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.NumberComparatorHelper;

/**
 * Validates both inclusive numeric bounds.
 */
public class RangeValidatorForDouble extends AbstractRangeValidator<Double> {

	@Override
	protected boolean isInRange(Double value) {
		return NumberComparatorHelper.compare( value, minValue, InfinityNumberComparatorHelper.LESS_THAN ) >= 0
				&& NumberComparatorHelper.compare( value, maxValue, InfinityNumberComparatorHelper.GREATER_THAN ) <= 0;
	}
}
