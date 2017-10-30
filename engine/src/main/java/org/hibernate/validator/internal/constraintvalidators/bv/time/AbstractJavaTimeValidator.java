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
import java.time.Duration;
import java.time.temporal.TemporalAccessor;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all time validators that are based on the {@code java.time} package.
 *
 * @author Alaa Nassef
 * @author Guillaume Smet
 */
public abstract class AbstractJavaTimeValidator<C extends Annotation, T extends TemporalAccessor & Comparable<? super T>> implements HibernateConstraintValidator<C, T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	protected Duration tolerance;

	@Override
	public void initialize(ConstraintDescriptor<C> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		this.tolerance = initializationContext.getClockSkewTolerance();
	}

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

		int result = value.compareTo( adjustedReferenceValue( getReferenceValue( reference ) ) );

		return isValid( result );
	}

	/**
	 * Returns an object of the validated type corresponding to the current time reference as provided by the
	 * {@link ClockProvider}.
	 */
	protected abstract T getReferenceValue(Clock reference);

	/**
	 * @param value a value to adjust
	 * @return an adjusted value based on {@link #tolerance} and constraint type
	 */
	protected abstract T adjustedReferenceValue(T value);

	/**
	 * Returns whether the result of the comparison between the validated value and the time reference is considered
	 * valid.
	 */
	protected abstract boolean isValid(int result);

}
