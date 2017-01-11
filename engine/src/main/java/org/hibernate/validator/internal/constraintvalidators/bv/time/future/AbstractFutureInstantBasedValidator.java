/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time.future;

import java.time.Instant;

import javax.validation.constraints.Future;

import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractInstantBasedTimeValidator;

/**
 * Base class for all {@code @Future} validators that use an {@link Instant} to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractFutureInstantBasedValidator<T> extends AbstractInstantBasedTimeValidator<Future, T> {

	private boolean isPresentValid;

	@Override
	public void initialize(Future constraintAnnotation) {
		isPresentValid = constraintAnnotation.orPresent();
	}

	@Override
	protected boolean isValid(int result) {
		return isPresentValid ? ( result >= 0 ) : ( result > 0 );
	}

}
