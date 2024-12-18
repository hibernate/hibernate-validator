/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.chrono.HijrahDate;

/**
 * Check that the {@code java.time.chrono.HijrahDate} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForHijrahDate extends AbstractPastOrPresentJavaTimeValidator<HijrahDate> {

	@Override
	protected HijrahDate getReferenceValue(Clock reference) {
		return HijrahDate.now( reference );
	}

}
