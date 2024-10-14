/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMaxValidatorForNumber extends AbstractDecimalMaxValidator<Number> {

	@Override
	protected int compare(Number number) {
		return DecimalNumberComparatorHelper.compare( number, maxValue, InfinityNumberComparatorHelper.GREATER_THAN );
	}
}
