/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.NotCompromised;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NotCompromisedValidatorTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( CompromisedPasswordChecker.class, new StubCompromisedPasswordChecker() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void safePasswordPasses() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "safePassword123!" ) );
		assertNoViolations( violations );
	}

	@Test
	public void compromisedPasswordFails() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "password" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotCompromised.class ) );
	}

	@Test
	public void charArrayNullIsValid() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void charArraySafePasswordPasses() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( "safePassword123!".toCharArray() ) );
		assertNoViolations( violations );
	}

	@Test
	public void charArrayCompromisedPasswordFails() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( "123456".toCharArray() ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotCompromised.class ) );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void missingCheckerThrows() {
		Validator noServiceValidator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();
		noServiceValidator.validate( new CharSequenceBean( "test" ) );
	}

	private static class CharSequenceBean {
		@NotCompromised
		private final String password;

		CharSequenceBean(String password) {
			this.password = password;
		}
	}

	private static class CharArrayBean {
		@NotCompromised
		private final char[] password;

		CharArrayBean(char[] password) {
			this.password = password;
		}
	}

	private static class StubCompromisedPasswordChecker implements CompromisedPasswordChecker {

		private static final Set<String> COMPROMISED = new HashSet<>( Arrays.asList(
				"password", "123456", "qwerty", "letmein"
		) );

		@Override
		public CompromisedPasswordResult check(char[] password) {
			boolean isCompromised = COMPROMISED.contains( new String( password ) );
			return new CompromisedPasswordResult() {
				@Override
				public boolean compromised() {
					return isCompromised;
				}

				@Override
				public int occurrences() {
					return isCompromised ? 1000 : 0;
				}

				@Override
				public void addMessageParameters(HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {

				}
			};
		}
	}
}
