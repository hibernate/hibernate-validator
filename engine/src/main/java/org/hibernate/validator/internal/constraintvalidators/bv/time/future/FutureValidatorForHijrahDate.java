/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.chrono.HijrahDate;

/**
 * Check that the {@code java.time.chrono.HijrahDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForHijrahDate extends AbstractFutureJavaTimeValidator<HijrahDate> {

	@Override
	protected HijrahDate getReferenceValue(Clock reference) {
		return HijrahDate.now( reference );
	}

}
