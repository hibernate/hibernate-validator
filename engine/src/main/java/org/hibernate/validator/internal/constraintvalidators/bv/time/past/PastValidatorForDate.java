/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Instant;
import java.util.Date;

/**
 * Check that the {@code java.util.Date} passed to be validated is in the
 * past.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class PastValidatorForDate extends AbstractPastInstantBasedValidator<Date> {

	@Override
	protected Instant getInstant(Date value) {
		// we don't use Date.toInstant() as it's not supported by java.sql.Date
		return Instant.ofEpochMilli( value.getTime() );
	}

}
