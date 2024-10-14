/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * Check that the {@code java.time.ZonedDateTime} passed is in the past.
 *
 * @author Khalid Alqinyah
 * @author Guillaume Smet
 */
public class PastValidatorForZonedDateTime extends AbstractPastJavaTimeValidator<ZonedDateTime> {

	@Override
	protected ZonedDateTime getReferenceValue(Clock reference) {
		return ZonedDateTime.now( reference );
	}

}
