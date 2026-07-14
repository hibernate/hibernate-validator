/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;

public class ModelWithPasswordPolicyConstraints {

	public static class TestPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 );
		}
	}

	@PasswordPolicy(TestPolicy.class)
	private String string;

	@PasswordPolicy(TestPolicy.class)
	private char[] charArray;

	@PasswordPolicy(TestPolicy.class)
	private Integer integer;
}
