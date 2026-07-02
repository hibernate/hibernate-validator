/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

public class PasswordPolicyValidatorForCharSequence implements HibernateConstraintValidator<PasswordPolicy, CharSequence> {

	private List<PasswordPolicyRule> rules;

	@Override
	public void initialize(ConstraintDescriptor<PasswordPolicy> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.rules = PasswordPolicyValidationHelper.buildRules( constraintDescriptor, initializationContext );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		char[] chars = PasswordPolicyValidationHelper.toCharArray( value );
		try {
			return PasswordPolicyValidationHelper.validate( PasswordPolicyValidationHelper.createContext( chars ), rules, context );
		}
		finally {
			Arrays.fill( chars, '\0' );
		}
	}
}
