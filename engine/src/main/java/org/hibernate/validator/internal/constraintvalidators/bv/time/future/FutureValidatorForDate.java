/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Instant;
import java.util.Date;

/**
 * Check that the {@code java.util.Date} passed to be validated is in the
 * future.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class FutureValidatorForDate extends AbstractFutureInstantBasedValidator<Date> {

	@Override
	protected Instant getInstant(Date value) {
		// we don't use Date.toInstant() as it's not supported by java.sql.Date
		return Instant.ofEpochMilli( value.getTime() );
	}

}
