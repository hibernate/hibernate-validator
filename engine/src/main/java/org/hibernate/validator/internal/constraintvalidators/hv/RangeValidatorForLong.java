/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

/**
 * Validates both inclusive integral bounds.
 */
public class RangeValidatorForLong extends AbstractRangeValidator<Long> {

	@Override
	protected boolean isInRange(Long value) {
		long number = value.longValue();
		return number >= minValue && number <= maxValue;
	}
}
