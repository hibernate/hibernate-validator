/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

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

public class PasswordStrengthValidatorForCharSequence implements HibernateConstraintValidator<PasswordStrength, CharSequence> {

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
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		char[] chars = toCharArray( value );
		try {
			PasswordStrengthResult result = estimator.estimate( chars );

			if ( result.meetsMinimumStrength( minScore ) ) {
				return true;
			}
			final HibernateConstraintValidatorContext hibernateConstraintValidatorContext = context.unwrap( HibernateConstraintValidatorContext.class );
			hibernateConstraintValidatorContext.addMessageParameter( "min", minScore );
			result.addMessageParameters( hibernateConstraintValidatorContext );
			return false;
		}
		finally {
			Arrays.fill( chars, '\0' );
		}
	}

	private static char[] toCharArray(CharSequence cs) {
		char[] chars = new char[cs.length()];
		for ( int i = 0; i < cs.length(); i++ ) {
			chars[i] = cs.charAt( i );
		}
		return chars;
	}
}
