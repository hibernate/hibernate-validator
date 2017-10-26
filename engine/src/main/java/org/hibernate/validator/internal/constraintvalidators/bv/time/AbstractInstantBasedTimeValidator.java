/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.time;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.Instant;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all time validators that use an {@link Instant} to be compared to the time reference.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractInstantBasedTimeValidator<C extends Annotation, T> implements ConstraintValidator<C, T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		Clock reference;

		try {
			ClockProvider clockProvider = context.getClockProvider();
			reference = clockProvider.getClock();
		}
		catch (Exception e) {
			throw LOG.getUnableToGetCurrentTimeFromClockProvider( e );
		}

		int result = getInstant( value ).compareTo( reference.instant() );

		return isValid( result );
	}

	/**
	 * Returns the {@link Instant} measured from Epoch.
	 */
	protected abstract Instant getInstant(T value);

	/**
	 * Returns whether the result of the comparison between the validated value and the time reference is considered
	 * valid.
	 */
	protected abstract boolean isValid(int result);

}
