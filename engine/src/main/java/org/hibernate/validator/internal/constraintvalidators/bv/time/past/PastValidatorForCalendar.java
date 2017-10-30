/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Instant;
import java.util.Calendar;

import javax.validation.constraints.Past;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractInstantBasedValidator;

/**
 * Check that the {@code java.util.Calendar} passed to be validated is in
 * the past.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public class PastValidatorForCalendar extends AbstractInstantBasedValidator<Past, Calendar> {

	@Override
	protected Instant getInstant(Calendar value) {
		return value.toInstant();
	}

}
