/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Check that the {@code java.time.OffsetDateTime} passed is in the past.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForOffsetDateTime extends AbstractPastOrPresentJavaTimeValidator<OffsetDateTime> {

	@Override
	protected OffsetDateTime getReferenceValue(Clock reference) {
		return OffsetDateTime.now( reference );
	}

}
