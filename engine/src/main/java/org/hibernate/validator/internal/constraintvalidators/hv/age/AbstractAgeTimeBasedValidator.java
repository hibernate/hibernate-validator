/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;

/**
 * Base class for all age validators that are based on the {@code java.time} package.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeTimeBasedValidator<C extends Annotation, T extends TemporalAccessor & Comparable<? super T>>
		implements HibernateConstraintValidator<C, T> {

	protected Clock referenceClock;

	protected int referenceAge;

	protected boolean inclusive;

	protected ChronoUnit unit;

	@Override
	public boolean isValid(T value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		long result =  this.getCurrentAge( value ) - this.referenceAge;

		return isValid( result );
	}

	/**
	 * Returns the temporal validation tolerance to apply.
	 */
	protected abstract Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance);

	/**
	 * Returns the number of Years, Days, Months, etc. according to an unit {@code java.time.temporal.ChronoUnit}
	 * from a given {@code java.util.Calendar} to current day
	 */
	protected abstract long getCurrentAge(T value);

	/**
	 * Returns whether the result of the comparison between the validated value and the age reference is considered
	 * valid.
	 */
	protected abstract boolean isValid(long result);
}
