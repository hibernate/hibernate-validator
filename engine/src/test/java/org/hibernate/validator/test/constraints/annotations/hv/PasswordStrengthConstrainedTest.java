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
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.password.PasswordStrengthScore;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PasswordStrengthConstrainedTest extends AbstractConstrainedTest {

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( PasswordStrengthEstimator.class, new LengthBasedEstimator() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void testValidPassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "strongPassword123" ) );
		assertNoViolations( violations );
	}

	@Test
	public void testInvalidPassword() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( "ab" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );
	}

	private static class Foo {

		@PasswordStrength
		private final String password;

		Foo(String password) {
			this.password = password;
		}
	}

	private static class LengthBasedEstimator implements PasswordStrengthEstimator {

		@Override
		public PasswordStrengthResult estimate(char[] password) {
			int score = password.length < 8 ? PasswordStrengthScore.WEAK : PasswordStrengthScore.STRONG;
			return new PasswordStrengthResult() {
				@Override
				public int score() {
					return score;
				}

				@Override
				public String feedback() {
					return null;
				}
			};
		}
	}
}
