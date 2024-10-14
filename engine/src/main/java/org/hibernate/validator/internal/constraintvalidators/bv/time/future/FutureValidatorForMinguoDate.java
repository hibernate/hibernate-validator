/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.chrono.MinguoDate;

/**
 * Check that the {@code java.time.chrono.MinguoDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForMinguoDate extends AbstractFutureJavaTimeValidator<MinguoDate> {

	@Override
	protected MinguoDate getReferenceValue(Clock reference) {
		return MinguoDate.now( reference );
	}

}
