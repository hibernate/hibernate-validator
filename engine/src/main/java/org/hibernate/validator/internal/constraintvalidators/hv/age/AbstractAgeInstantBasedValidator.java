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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all age validators that use an {@link Instant} to be compared to the age reference.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeInstantBasedValidator<C extends Annotation, T>
		implements HibernateConstraintValidator<C, T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private Clock referenceClock;

	private int referenceAge;

	private boolean inclusive;

	private ChronoUnit unit;

	public void initialize(
			int referenceAge,
			ChronoUnit unit,
			boolean inclusive,
			HibernateConstraintValidatorInitializationContext initializationContext) {
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
		// As Instant does not support plus operation on ChronoUnits greater than DAYS we need to convert it to LocalDate
		// first, which supports such operations.

		int result = getInstant( value ).atZone( ZoneOffset.ofHours( 0 ) ).toLocalDate()
				.compareTo( LocalDate.now( referenceClock ).minus( referenceAge, unit ) );

		return isValid( result );
	}

	/**
	 * Returns whether the specified value is inclusive or exclusive.
	 */
	protected boolean isInclusive() {
		return this.inclusive;
	}

	/**
	 * Returns the temporal validation tolerance to apply.
	 */
	protected abstract Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance);

	/**
	 * Returns the {@link Instant} measured from Epoch.
	 */
	protected abstract Instant getInstant(T value);

	/**
	 * Returns whether the result of the comparison between the validated value and the reference age is considered
	 * valid.
	 */
	protected abstract boolean isValid(int result);

}
