/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Clock;
import java.time.LocalDateTime;

import javax.validation.constraints.PastOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractTemporalValidator;

/**
 * Check that the {@code java.time.LocalDateTime} passed is in the past.
 *
 * @author Guillaume Smet
 */
public class PastOrPresentValidatorForLocalDateTime extends AbstractTemporalValidator<PastOrPresent, LocalDateTime> {

	@Override
	protected LocalDateTime getReferenceValue(Clock reference) {
		return LocalDateTime.now( reference );
	}

}
