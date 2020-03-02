/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent;

import java.time.Duration;

import jakarta.validation.constraints.PastOrPresent;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractEpochBasedTimeValidator;

/**
 * Base class for all {@code @PastOrPresent} validators that use an epoch to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractPastOrPresentEpochBasedValidator<T> extends AbstractEpochBasedTimeValidator<PastOrPresent, T> {

	@Override
	protected boolean isValid(int result) {
		return result <= 0;
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance;
	}
}
