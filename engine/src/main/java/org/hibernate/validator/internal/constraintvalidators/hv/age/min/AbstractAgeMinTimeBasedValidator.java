/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.constraintvalidators.hv.age.AbstractAgeTimeBasedValidator;

/**
 * Base class for all {@code @AgeMin} validators that are based on the {@code java.time} package.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public abstract class AbstractAgeMinTimeBasedValidator<T extends Temporal & TemporalAccessor & Comparable<? super T>>
		extends AbstractAgeTimeBasedValidator<AgeMin, T> {

	@Override
	public void initialize(
			ConstraintDescriptor<AgeMin> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		super.initialize( constraintDescriptor.getAnnotation().value(), constraintDescriptor.getAnnotation().unit(),
						  constraintDescriptor.getAnnotation().inclusive(), initializationContext
		);
	}

	@Override
	protected Duration getEffectiveTemporalValidationTolerance(Duration absoluteTemporalValidationTolerance) {
		return absoluteTemporalValidationTolerance.negated();
	}

	@Override
	protected boolean isValid(long result) {
		return super.inclusive ? result <= 0 : result < 0;
	}

}
