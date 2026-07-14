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
import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.constraints.NotCompromised;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NotCompromisedConstrainedTest extends AbstractConstrainedTest {

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						CompromisedPasswordChecker.class,
						BeanReference.ofInstance( new StubChecker() ) ) )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void testSafePassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "safePassword123!" ) );
		assertNoViolations( violations );
	}

	@Test
	public void testCompromisedPassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "password" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotCompromised.class ) );
	}

	private static class Foo {

		@NotCompromised
		private final String password;

		Foo(String password) {
			this.password = password;
		}
	}

	private static class StubChecker implements CompromisedPasswordChecker {

		private static final Set<String> COMPROMISED = Set.of( "password", "123456" );

		@Override
		public CompromisedPasswordResult check(char[] password) {
			boolean found = COMPROMISED.contains( new String( password ) );
			return new CompromisedPasswordResult() {
				@Override
				public boolean compromised() {
					return found;
				}

				@Override
				public int occurrences() {
					return -1;
				}
			};
		}
	}
}
