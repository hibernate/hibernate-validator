/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * Check that the {@code java.time.ZonedDateTime} passed is in the future.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForZonedDateTime extends AbstractFutureOrPresentJavaTimeValidator<ZonedDateTime> {

	@Override
	protected ZonedDateTime getReferenceValue(Clock reference) {
		return ZonedDateTime.now( reference );
	}

}
