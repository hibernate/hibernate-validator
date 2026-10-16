/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

/**
 * Validates both inclusive integral bounds.
 */
public class RangeValidatorForInteger extends AbstractRangeValidator<Integer> {

	@Override
	protected boolean isInRange(Integer value) {
		long number = value.longValue();
		return number >= minValue && number <= maxValue;
	}
}
