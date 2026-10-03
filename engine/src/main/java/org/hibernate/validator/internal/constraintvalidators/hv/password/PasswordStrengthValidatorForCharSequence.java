/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.Arrays;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

public class PasswordStrengthValidatorForCharSequence implements HibernateConstraintValidator<PasswordStrength, CharSequence> {

	private int minScore;
	private BeanHolder<PasswordStrengthEstimator> estimatorHolder;

	@Override
	public void initialize(ConstraintDescriptor<PasswordStrength> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.minScore = constraintDescriptor.getAnnotation().min();
		String estimatorRef = constraintDescriptor.getAnnotation().estimator();
		if ( estimatorRef.isEmpty() ) {
			this.estimatorHolder = initializationContext.getBeanResolver()
					.resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY );
		}
		else {
			this.estimatorHolder = initializationContext.getBeanResolver()
					.resolve( BeanReference.parse( PasswordStrengthEstimator.class, estimatorRef ) );
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		char[] chars = PasswordPolicyValidationHelper.toCharArray( value );
		try {
			PasswordStrengthResult result = estimatorHolder.get().estimate( chars );

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

	@Override
	public void close() {
		if ( estimatorHolder != null ) {
			estimatorHolder.close();
		}
	}
}
