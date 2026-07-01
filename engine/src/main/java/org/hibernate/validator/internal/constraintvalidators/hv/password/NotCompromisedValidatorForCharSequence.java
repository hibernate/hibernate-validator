/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

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

public class NotCompromisedValidatorForCharSequence implements HibernateConstraintValidator<NotCompromised, CharSequence> {

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
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		char[] chars = PasswordPolicyValidationHelper.toCharArray( value );
		try {
			final CompromisedPasswordResult result = checker.check( chars );
			if ( !result.compromised() ) {
				return true;
			}
			result.addMessageParameters( context.unwrap( HibernateConstraintValidatorContext.class ) );
			return false;
		}
		finally {
			Arrays.fill( chars, '\0' );
		}
	}
}
