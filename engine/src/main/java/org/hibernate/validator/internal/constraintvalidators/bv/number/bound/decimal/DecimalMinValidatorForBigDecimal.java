/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal;

import java.math.BigDecimal;

/**
 * Check that the number being validated is greater than or equal to the minimum
 * value specified.
 *
 * @author Marko Bekhta
 */
public class DecimalMinValidatorForBigDecimal extends AbstractDecimalMinValidator<BigDecimal> {

	@Override
	protected int compare(BigDecimal number) {
		return DecimalNumberComparatorHelper.compare( number, minValue );
	}
}
