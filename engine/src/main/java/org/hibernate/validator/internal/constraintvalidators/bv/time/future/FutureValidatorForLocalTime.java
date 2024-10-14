/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.LocalTime;

/**
 * Check that the {@code java.time.LocalTime} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForLocalTime extends AbstractFutureJavaTimeValidator<LocalTime> {

	@Override
	protected LocalTime getReferenceValue(Clock reference) {
		return LocalTime.now( reference );
	}

}
