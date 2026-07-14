/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

public class PasswordPolicyValidationHelper {

	private PasswordPolicyValidationHelper() {
	}

	public static BeanHolder<List<PasswordPolicyRule>> buildRules(ConstraintDescriptor<PasswordPolicy> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		Class<? extends PasswordPolicyDefinition> definitionClass = constraintDescriptor.getAnnotation().value();

		BeanHolder<? extends PasswordPolicyDefinition> definitionHolder = initializationContext.getBeanResolver()
				.resolve( definitionClass, BeanRetrieval.ANY );

		return new PasswordPolicyRulesBeanHolder( definitionHolder, initializationContext );
	}

	public static DefaultPasswordContext createContext(char[] password) {
		return new DefaultPasswordContext( password );
	}

	static char[] toCharArray(CharSequence cs) {
		char[] chars = new char[cs.length()];
		for ( int i = 0; i < cs.length(); i++ ) {
			chars[i] = cs.charAt( i );
		}
		return chars;
	}

	public static boolean validate(PasswordContext passwordContext, List<PasswordPolicyRule> rules, ConstraintValidatorContext context) {
		HibernateConstraintValidatorContext hvContext = context.unwrap( HibernateConstraintValidatorContext.class );

		boolean allValid = true;
		for ( PasswordPolicyRule rule : rules ) {
			if ( !rule.isValid( passwordContext, hvContext ) ) {
				if ( allValid ) {
					hvContext.disableDefaultConstraintViolation();
					allValid = false;
				}
				hvContext.buildConstraintViolationWithTemplate( rule.getMessage() )
						.addConstraintViolation();
			}
		}

		return allValid;
	}

	private static class PasswordPolicyRulesBeanHolder implements BeanHolder<List<PasswordPolicyRule>> {
		private final BeanHolder<? extends PasswordPolicyDefinition> definitionHolder;
		private final HibernateConstraintValidatorInitializationContext initializationContext;
		private volatile List<PasswordPolicyRule> rules;

		public PasswordPolicyRulesBeanHolder(
				BeanHolder<? extends PasswordPolicyDefinition> definitionHolder,
				HibernateConstraintValidatorInitializationContext initializationContext) {
			this.definitionHolder = definitionHolder;
			this.initializationContext = initializationContext;
		}

		@Override
		public List<PasswordPolicyRule> get() {
			List<PasswordPolicyRule> result = rules;
			if ( result == null ) {
				synchronized (this) {
					result = rules;
					if ( result == null ) {
						DefaultPasswordPolicyBuilder builder = new DefaultPasswordPolicyBuilder();
						definitionHolder.get().configure( builder, initializationContext );
						result = builder.build();
						rules = result;
					}
				}
			}
			return result;
		}

		@Override
		public void close() {
			definitionHolder.close();
		}
	}
}
