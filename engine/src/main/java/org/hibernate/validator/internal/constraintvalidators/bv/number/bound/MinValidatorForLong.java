/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class MinValidatorForLong extends AbstractMinValidator<Long> {

	@Override
	protected int compare(Long number) {
		return NumberComparatorHelper.compare( number, minValue );
	}
}
