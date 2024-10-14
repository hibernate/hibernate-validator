/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.YearMonth;

/**
 * Check that the {@code java.time.YearMonth} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForYearMonth extends AbstractPastOrPresentJavaTimeValidator<YearMonth> {

	@Override
	protected YearMonth getReferenceValue(Clock reference) {
		return YearMonth.now( reference );
	}

}
