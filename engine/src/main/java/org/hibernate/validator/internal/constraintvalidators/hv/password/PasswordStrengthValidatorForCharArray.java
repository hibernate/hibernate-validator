/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

public class PasswordStrengthValidatorForCharArray implements HibernateConstraintValidator<PasswordStrength, char[]> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int minScore;
	private PasswordStrengthEstimator estimator;

	@Override
	public void initialize(ConstraintDescriptor<PasswordStrength> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.minScore = constraintDescriptor.getAnnotation().min();
		this.estimator = initializationContext.getValidationService( PasswordStrengthEstimator.class );
		if ( this.estimator == null ) {
			throw LOG.getNoPasswordStrengthEstimatorException();
		}
	}

	@Override
	public boolean isValid(char[] value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		PasswordStrengthResult result = estimator.estimate( value );

		if ( result.meetsMinimumStrength( minScore ) ) {
			return true;
		}
		final HibernateConstraintValidatorContext hibernateConstraintValidatorContext = context.unwrap( HibernateConstraintValidatorContext.class );
		hibernateConstraintValidatorContext.addMessageParameter( "min", minScore );
		result.addMessageParameters( hibernateConstraintValidatorContext );
		return false;
	}
}
