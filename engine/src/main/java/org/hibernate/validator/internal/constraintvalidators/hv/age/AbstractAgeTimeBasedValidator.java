/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all age validators that are based on the {@code java.time} package.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeTimeBasedValidator<C extends Annotation, T extends TemporalAccessor & Comparable<? super T>>
		implements HibernateConstraintValidator<C, T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private Clock referenceClock;

	protected int referenceAge;

	protected boolean inclusive;

	protected ChronoUnit unit;

	public void initialize(int referenceAge, ChronoUnit unit, boolean inclusive, HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			this.referenceClock = Clock.offset(
					initializationContext.getClockProvider().getClock(),
					getEffectiveTemporalValidationTolerance( initializationContext.getTemporalValidationTolerance() )
			);
		}
		catch (Exception e) {
			throw LOG.getUnableToGetCurrentTimeFromClockProvider( e );
		}
		this.referenceAge = referenceAge;
		this.unit = unit;
		this.inclusive = inclusive;
	}

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		int result = value.compareTo( getReferenceValue( referenceClock, referenceAge, unit ) );

		return isValid( result );
	}

	/**
	 * Returns the temporal validation tolerance to apply.
	 */
	protected abstract Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance);

	/**
	 * Returns an object of the validated type corresponding to the time reference as provided by the
	 * {@link ClockProvider} increased or decreased with the specified referenceAge of Years/Days/Months/etc.
	 * defined by {@link ChronoUnit}.
	 */
	protected abstract T getReferenceValue(Clock reference, int referenceAge, ChronoUnit unit );

	/**
	 * Returns whether the result of the comparison between the validated value and the age reference is considered
	 * valid.
	 */
	protected abstract boolean isValid(long result);

}
