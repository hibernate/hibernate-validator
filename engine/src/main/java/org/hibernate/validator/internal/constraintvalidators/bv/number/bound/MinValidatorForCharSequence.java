/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.number.bound;

import java.math.BigDecimal;

/**
 * Check that the character sequence (e.g. string) being validated represents a number, and has a value
 * more than or equal to the minimum value specified.
 *
 * @author Alaa Nassef
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class MinValidatorForCharSequence extends AbstractMinValidator<CharSequence> {

	@Override
	protected int compare(CharSequence number) {
		try {
			return NumberComparatorHelper.compare( new BigDecimal( number.toString() ), minValue );
		}
		catch (NumberFormatException nfe) {
			return -1;
		}
	}
}
