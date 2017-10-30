/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Clock;
import java.time.LocalDate;

import javax.validation.constraints.Future;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.LocalDate} passed is in the future.
 *
 * @author Guillaume Smet
 */
public class FutureValidatorForLocalDate extends AbstractTemporalValidator<Future, LocalDate> {

	@Override
	protected LocalDate getReferenceValue(Clock reference) {
		return LocalDate.now( reference );
	}

}
