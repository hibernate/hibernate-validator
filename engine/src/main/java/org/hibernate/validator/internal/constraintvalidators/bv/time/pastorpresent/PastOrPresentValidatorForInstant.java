/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.Instant;

/**
 * Check that the {@code java.time.Instant} passed is in the past.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForInstant extends AbstractPastOrPresentJavaTimeValidator<Instant> {

	@Override
	protected Instant getReferenceValue(Clock reference) {
		return Instant.now( reference );
	}

}
