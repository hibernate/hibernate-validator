/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.past;

import javax.validation.constraints.Past;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractEpochBasedTimeValidator;

/**
 * Base class for all {@code @Past} validators that use an epoch to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractPastEpochBasedValidator<T> extends AbstractEpochBasedTimeValidator<Past, T> {

	private boolean isPresentValid;

	@Override
	public void initialize(Past constraintAnnotation) {
		isPresentValid = constraintAnnotation.orPresent();
	}

	@Override
	protected boolean isValid(int result) {
		return isPresentValid ? ( result <= 0 ) : ( result < 0 );
	}

}
