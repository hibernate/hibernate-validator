/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.chrono.ThaiBuddhistDate;

/**
 * Check that the {@code java.time.chrono.ThaiBuddhistDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForThaiBuddhistDate extends AbstractFutureOrPresentJavaTimeValidator<ThaiBuddhistDate> {

	@Override
	protected ThaiBuddhistDate getReferenceValue(Clock reference) {
		return ThaiBuddhistDate.now( reference );
	}

}
