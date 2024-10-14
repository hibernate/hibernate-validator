/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.MonthDay;

/**
 * Check that the {@code java.time.MonthDay} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForMonthDay extends AbstractPastJavaTimeValidator<MonthDay> {

	@Override
	protected MonthDay getReferenceValue(Clock reference) {
		return MonthDay.now( reference );
	}

}
