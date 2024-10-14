/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMinValidatorForByte extends AbstractDecimalMinValidator<Byte> {

	@Override
	protected int compare(Byte number) {
		return DecimalNumberComparatorHelper.compare( number.longValue(), minValue );
	}
}
