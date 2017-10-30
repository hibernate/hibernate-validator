/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Base class for all {@code @Future} validators that use an {@link Instant} to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractInstantBasedValidator<C extends Annotation, T> extends AbstractJavaTimeValidator<C, T> {

	@Override
	protected Duration getDifference(Clock reference, T value) {
		return Duration.between( getInstant( value ), adjustCurrentTime( reference.instant() ) );
	}

	/**
	 * Returns the {@link Instant} measured from Epoch.
	 */
	protected abstract Instant getInstant(T value);

}
