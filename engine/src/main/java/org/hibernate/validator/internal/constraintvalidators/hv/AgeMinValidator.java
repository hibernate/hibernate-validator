/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;

import java.lang.invoke.MethodHandles;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 *
 * Checks that the number of years from a given date to today is greater or equal to
 * a specified value
 *
 * @author Hillmer Chona
 */
public class AgeMinValidator implements HibernateConstraintValidator<AgeMin, LocalDate> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int minAge;

	private boolean inclusive;

	protected Clock referenceClock;

	@Override
	public void initialize(ConstraintDescriptor<AgeMin> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			this.referenceClock  = Clock.offset(
					initializationContext.getClockProvider().getClock(),
					getEffectiveTemporalValidationTolerance( initializationContext.getTemporalValidationTolerance() )
			);
			this.minAge = constraintDescriptor.getAnnotation().value();
			this.inclusive = constraintDescriptor.getAnnotation().inclusive();

		}
		catch (Exception e) {
			throw LOG.getUnableToGetCurrentTimeFromClockProvider( e );
		}
	}

	@Override
	public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
		// null values are valid
		if ( date == null ) {
			return true;
		}
		return inclusive ? ChronoUnit.YEARS.between( date, getReferenceValue( referenceClock ) ) >= minAge : ChronoUnit.YEARS.between( date, getReferenceValue( referenceClock ) ) > minAge;
	}

	private LocalDate getReferenceValue(Clock reference) {
		return LocalDate.now( reference );
	}

	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance.negated();
	}
}
