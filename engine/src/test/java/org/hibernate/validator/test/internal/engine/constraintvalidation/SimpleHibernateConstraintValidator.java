/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import java.time.Duration;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

public class SimpleHibernateConstraintValidator implements HibernateConstraintValidator<SimpleHibernateConstraintValidatorConstraint, String> {

	private Duration duration;

	@Override
	public void initialize(ConstraintDescriptor<SimpleHibernateConstraintValidatorConstraint> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		duration = initializationContext.getTemporalValidationTolerance();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		context.buildConstraintViolationWithTemplate( duration.toString() ).addConstraintViolation().disableDefaultConstraintViolation();
		return false;
	}
}
