/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import org.hibernate.validator.internal.constraintvalidators.bv.number.InfinityNumberComparatorHelper;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class MinValidatorForFloat extends AbstractMinValidator<Float> {

	@Override
	protected int compare(Float number) {
		return NumberComparatorHelper.compare( number, minValue, InfinityNumberComparatorHelper.LESS_THAN );
	}
}
