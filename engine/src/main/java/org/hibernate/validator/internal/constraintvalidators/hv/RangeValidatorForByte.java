/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

/**
 * Validates both inclusive integral bounds.
 */
public class RangeValidatorForByte extends AbstractRangeValidator<Byte> {

	@Override
	protected boolean isInRange(Byte value) {
		long number = value.longValue();
		return number >= minValue && number <= maxValue;
	}
}
