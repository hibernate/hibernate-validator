/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.NotCompromised;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.password.PasswordStrengthScore;

import org.junit.Ignore;
import org.junit.Test;

public class PasswordValidationServicesTest {

	@Test
	public void zxcvbn4jWeakPassword() {
		Zxcvbn4jPasswordStrengthEstimator estimator = new Zxcvbn4jPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "password".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() < PasswordStrengthScore.STRONG );
	}

	@Test
	public void zxcvbn4jStrongPassword() {
		Zxcvbn4jPasswordStrengthEstimator estimator = new Zxcvbn4jPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "c0rr3ct-h0rs3-b4tt3ry-st4pl3!".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() >= PasswordStrengthScore.STRONG );
	}

	@Ignore("Requires network access to the Have I Been Pwned API")
	@Test
	public void haveIBeenPwnedCompromisedPassword() {
		HaveIBeenPwnedPasswordChecker checker = new HaveIBeenPwnedPasswordChecker();
		assertTrue( checker.check( "password".toCharArray() ).compromised() );
	}

	@Ignore("Requires network access to the Have I Been Pwned API")
	@Test
	public void haveIBeenPwnedSafePassword() {
		HaveIBeenPwnedPasswordChecker checker = new HaveIBeenPwnedPasswordChecker();
		assertFalse( checker.check( "c0rr3ct-h0rs3-b4tt3ry-st4pl3!2025xyz".toCharArray() ).compromised() );
	}

	@Test
	public void passwordStrengthValidation() {
		//tag::weightRegistration[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( PasswordStrengthEstimator.class,
						new Zxcvbn4jPasswordStrengthEstimator() )
				.buildValidatorFactory()
				.getValidator();
		//end::weightRegistration[]

		Set<ConstraintViolation<UserAccount>> violations = validator.validate(
				new UserAccount( "password" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordStrength.class ) );

		violations = validator.validate(
				new UserAccount( "c0rr3ct-h0rs3-b4tt3ry-st4pl3!" ) );
		assertNoViolations( violations );
	}

	@Ignore("Requires network access to the Have I Been Pwned API")
	@Test
	public void notCompromisedValidation() {
		//tag::compromisedRegistration[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( CompromisedPasswordChecker.class,
						new HaveIBeenPwnedPasswordChecker() )
				.buildValidatorFactory()
				.getValidator();
		//end::compromisedRegistration[]

		Set<ConstraintViolation<SecureAccount>> violations = validator.validate(
				new SecureAccount( "password" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotCompromised.class ) );
	}

	public static class UserAccount {

		@PasswordStrength
		private final String password;

		UserAccount(String password) {
			this.password = password;
		}
	}

	public static class SecureAccount {

		@NotCompromised
		private final String password;

		SecureAccount(String password) {
			this.password = password;
		}
	}
}
