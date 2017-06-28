/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent;

import java.time.Instant;
import java.util.Date;

/**
 * Check that the {@code java.util.Date} passed to be validated is in the
 * future.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class FutureOrPresentValidatorForDate extends AbstractFutureOrPresentInstantBasedValidator<Date> {

	@Override
	protected Instant getInstant(Date value) {
		return value.toInstant();
	}

}
