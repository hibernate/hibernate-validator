/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.constraints.NotCompromised;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;

public class NotCompromisedValidatorForCharArray implements HibernateConstraintValidator<NotCompromised, char[]> {

	private CompromisedPasswordChecker checker;

	@Override
	public void initialize(ConstraintDescriptor<NotCompromised> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		String checkerRef = constraintDescriptor.getAnnotation().checker();
		if ( checkerRef.isEmpty() ) {
			this.checker = initializationContext.getBeanResolver()
					.resolve( CompromisedPasswordChecker.class, BeanRetrieval.ANY ).get();
		}
		else {
			this.checker = initializationContext.getBeanResolver()
					.resolve( BeanReference.parse( CompromisedPasswordChecker.class, checkerRef ) ).get();
		}
	}

	@Override
	public boolean isValid(char[] value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		final CompromisedPasswordResult result = checker.check( value );
		if ( !result.compromised() ) {
			return true;
		}
		result.addMessageParameters( context.unwrap( HibernateConstraintValidatorContext.class ) );
		return false;
	}
}
