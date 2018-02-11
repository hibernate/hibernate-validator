/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.time.Instant;

import java.util.Date;

/**
 *  Checks that the number of Years, Days, Months, etc. according to an unit {@code java.time.temporal.ChronoUnit}
 *  from a given {@code java.util.Calendar} to current day is greater than or equal to the specified value if inclusive is true
 *  or is greater when inclusive is false.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public class AgeMinValidatorForDate extends AbstractAgeMinInstantBasedValidator<Date> {

	@Override
	protected Instant getInstant(Date value) {
		return value.toInstant();
	}
}
