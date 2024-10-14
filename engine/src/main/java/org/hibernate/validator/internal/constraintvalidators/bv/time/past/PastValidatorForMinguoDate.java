/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.chrono.MinguoDate;

/**
 * Check that the {@code java.time.chrono.MinguoDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastValidatorForMinguoDate extends AbstractPastJavaTimeValidator<MinguoDate> {

	@Override
	protected MinguoDate getReferenceValue(Clock reference) {
		return MinguoDate.now( reference );
	}

}
