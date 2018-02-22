/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.time.Duration;
import java.time.Instant;

import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.constraintvalidators.hv.age.AbstractAgeInstantBasedValidator;


/**
 * Base class for all {@code @AgeMin} validators that use an {@link Instant} to be compared to the age reference.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeMinInstantBasedValidator<T> extends AbstractAgeInstantBasedValidator<AgeMin, T>  {

	@Override
	public void initialize(ConstraintDescriptor<AgeMin> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		super.initialize( constraintDescriptor.getAnnotation().value(), constraintDescriptor.getAnnotation().unit().getChronoUnit(),
						  constraintDescriptor.getAnnotation().inclusive(), initializationContext );
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance;
	}

	@Override
	protected boolean isValid(int result) {
		return isInclusive() ? result <= 0 : result < 0;
	}
}
