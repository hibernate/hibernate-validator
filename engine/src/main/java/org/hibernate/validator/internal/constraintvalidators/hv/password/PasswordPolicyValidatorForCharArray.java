/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

public class PasswordPolicyValidatorForCharArray implements HibernateConstraintValidator<PasswordPolicy, char[]> {

	private BeanHolder<List<PasswordPolicyRule>> rulesHolder;

	@Override
	public void initialize(ConstraintDescriptor<PasswordPolicy> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.rulesHolder = PasswordPolicyValidationHelper.buildRules( constraintDescriptor, initializationContext );
	}

	@Override
	public boolean isValid(char[] value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		return PasswordPolicyValidationHelper.validate( PasswordPolicyValidationHelper.createContext( value ), rulesHolder.get(), context );
	}

	@Override
	public void close() {
		if ( rulesHolder != null ) {
			rulesHolder.close();
		}
	}
}
