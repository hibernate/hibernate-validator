/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

public class PasswordStrengthValidatorForCharArray implements HibernateConstraintValidator<PasswordStrength, char[]> {

	private int minScore;
	private PasswordStrengthEstimator estimator;

	@Override
	public void initialize(ConstraintDescriptor<PasswordStrength> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.minScore = constraintDescriptor.getAnnotation().min();
		String estimatorRef = constraintDescriptor.getAnnotation().estimator();
		if ( estimatorRef.isEmpty() ) {
			this.estimator = initializationContext.getBeanResolver()
					.resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY ).get();
		}
		else {
			this.estimator = initializationContext.getBeanResolver()
					.resolve( BeanReference.parse( PasswordStrengthEstimator.class, estimatorRef ) ).get();
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
