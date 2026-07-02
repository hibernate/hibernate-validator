/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.NotCompromised;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;

public class NotCompromisedValidatorForCharArray implements HibernateConstraintValidator<NotCompromised, char[]> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private CompromisedPasswordChecker checker;

	@Override
	public void initialize(ConstraintDescriptor<NotCompromised> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.checker = initializationContext.getValidationService( CompromisedPasswordChecker.class );
		if ( this.checker == null ) {
			throw LOG.getNoCompromisedPasswordCheckerException();
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
