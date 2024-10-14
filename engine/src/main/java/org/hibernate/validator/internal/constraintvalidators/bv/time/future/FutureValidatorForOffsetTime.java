/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.OffsetTime;

/**
 * Check that the {@code java.time.OffsetTime} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForOffsetTime extends AbstractFutureJavaTimeValidator<OffsetTime> {

	@Override
	protected OffsetTime getReferenceValue(Clock reference) {
		return OffsetTime.now( reference );
	}

}
