/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import java.time.Duration;
import java.time.Instant;

import jakarta.validation.constraints.Past;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractInstantBasedTimeValidator;

/**
 * Base class for all {@code @Past} validators that use an {@link Instant} to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractPastInstantBasedValidator<T> extends AbstractInstantBasedTimeValidator<Past, T> {

	@Override
	protected boolean isValid(int result) {
		return result < 0;
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance;
	}
}
