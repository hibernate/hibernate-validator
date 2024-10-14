/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.OffsetTime;

/**
 * Check that the {@code java.time.OffsetTime} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForOffsetTime extends AbstractPastOrPresentJavaTimeValidator<OffsetTime> {

	@Override
	protected OffsetTime getReferenceValue(Clock reference) {
		return OffsetTime.now( reference );
	}

}
