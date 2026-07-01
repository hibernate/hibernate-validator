/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

public class PasswordPolicyConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testValidPassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "Passw0rd!xyz" ) );
		assertNoViolations( violations );
	}

	@Test
	public void testInvalidPassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "ab" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	public static class SimplePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.requireCharacters( CharacterType.UPPERCASE, 1 )
					.requireCharacters( CharacterType.DIGIT, 1 );
		}
	}

	private static class Foo {

		@PasswordPolicy(SimplePolicy.class)
		private final String password;

		Foo(String password) {
			this.password = password;
		}
	}
}
