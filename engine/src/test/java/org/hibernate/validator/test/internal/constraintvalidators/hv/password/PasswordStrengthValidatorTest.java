/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.constraints.PasswordStrength;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.password.PasswordStrengthScore;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PasswordStrengthValidatorTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						PasswordStrengthEstimator.class,
						BeanReference.ofInstance( new StubPasswordStrengthEstimator() ) ) )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void weakPasswordFails() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "a" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );
	}

	@Test
	public void strongPasswordPasses() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "abcdefghijklmnop" ) );
		assertNoViolations( violations );
	}

	@Test
	public void charArrayNullIsValid() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void charArrayWeakPasswordFails() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( "a".toCharArray() ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );
	}

	@Test
	public void charArrayStrongPasswordPasses() {
		Set<ConstraintViolation<CharArrayBean>> violations = validator.validate( new CharArrayBean( "abcdefghijklmnop".toCharArray() ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyStringIsValidated() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );
	}

	@Test
	public void veryWeakPasswordScoreZeroFails() {
		Set<ConstraintViolation<WeakMinBean>> violations = validator.validate( new WeakMinBean( "" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );
	}

	@Test
	public void passwordAtExactMinScorePasses() {
		Set<ConstraintViolation<CharSequenceBean>> violations = validator.validate( new CharSequenceBean( "abcd" ) );
		assertNoViolations( violations );
	}

	@Test
	public void customMinScore() {
		Set<ConstraintViolation<CustomMinBean>> violations = validator.validate( new CustomMinBean( "abcd" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordStrength.class ) );

		violations = validator.validate( new CustomMinBean( "abcdefghijklmnop" ) );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void missingEstimatorThrows() {
		Validator noServiceValidator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();
		noServiceValidator.validate( new CharSequenceBean( "test" ) );
	}

	private static class CharSequenceBean {
		@PasswordStrength
		private final String password;

		CharSequenceBean(String password) {
			this.password = password;
		}
	}

	private static class CharArrayBean {
		@PasswordStrength
		private final char[] password;

		CharArrayBean(char[] password) {
			this.password = password;
		}
	}

	private static class WeakMinBean {
		@PasswordStrength(min = PasswordStrengthScore.WEAK)
		private final String password;

		WeakMinBean(String password) {
			this.password = password;
		}
	}

	private static class CustomMinBean {
		@PasswordStrength(min = PasswordStrengthScore.VERY_STRONG)
		private final String password;

		CustomMinBean(String password) {
			this.password = password;
		}
	}

	private static class StubPasswordStrengthEstimator implements PasswordStrengthEstimator {
		@Override
		public PasswordStrengthResult estimate(char[] password) {
			int score;
			if ( password.length < 2 ) {
				score = PasswordStrengthScore.VERY_WEAK;
			}
			else if ( password.length < 4 ) {
				score = PasswordStrengthScore.WEAK;
			}
			else if ( password.length < 8 ) {
				score = PasswordStrengthScore.FAIR;
			}
			else if ( password.length < 12 ) {
				score = PasswordStrengthScore.STRONG;
			}
			else {
				score = PasswordStrengthScore.VERY_STRONG;
			}
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
