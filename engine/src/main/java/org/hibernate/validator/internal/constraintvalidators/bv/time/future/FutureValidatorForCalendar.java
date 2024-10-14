/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Instant;
import java.util.Calendar;

/**
 * Check that the {@code java.util.Calendar} passed to be validated is in
 * the future.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class FutureValidatorForCalendar extends AbstractFutureInstantBasedValidator<Calendar> {

	@Override
	protected Instant getInstant(Calendar value) {
		return value.toInstant();
	}

}
