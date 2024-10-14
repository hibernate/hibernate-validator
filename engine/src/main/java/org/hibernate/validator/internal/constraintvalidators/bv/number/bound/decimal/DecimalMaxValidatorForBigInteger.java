/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import java.math.BigInteger;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMaxValidatorForBigInteger extends AbstractDecimalMaxValidator<BigInteger> {

	@Override
	protected int compare(BigInteger number) {
		return DecimalNumberComparatorHelper.compare( number, maxValue );
	}
}
