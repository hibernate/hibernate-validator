/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.constraintvalidators.hv.age.AbstractAgeInstantBasedValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for all {@code @AgeMin} validators that use an {@link Instant} to be compared to the age reference.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeMinInstantBasedValidator<T> extends AbstractAgeInstantBasedValidator<AgeMin, T>  {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Override
	public void initialize(ConstraintDescriptor<AgeMin> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			super.referenceClock  = Clock.offset(
					initializationContext.getClockProvider().getClock(),
					getEffectiveTemporalValidationTolerance( initializationContext.getTemporalValidationTolerance() )
			);
			super.referenceAge = constraintDescriptor.getAnnotation().value();
			super.inclusive = constraintDescriptor.getAnnotation().inclusive();
			super.unit = constraintDescriptor.getAnnotation().unit();

		}
		catch (Exception e) {
			throw LOG.getUnableToGetCurrentTimeFromClockProvider( e );
		}
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance;
	}

	@Override
	protected boolean isValid(long result) {
		return super.inclusive ? result >= 0 : result > 0;
	}
}
