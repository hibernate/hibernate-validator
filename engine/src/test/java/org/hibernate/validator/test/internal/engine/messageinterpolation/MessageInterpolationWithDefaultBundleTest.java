/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for correct message interpolation for messages from the default bundle.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class MessageInterpolationWithDefaultBundleTest {
	private Locale defaultLocale;

	@BeforeClass
	public void storeDefaultLocale() {
		defaultLocale = Locale.getDefault();
	}

	@AfterClass
	public void restoreDefaultLocale() {
		Locale.setDefault( defaultLocale );
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageEnglishLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.ENGLISH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "must be a well-formed email address" ),
				violationOf( Range.class ).withMessage( "must be between 18 and 21" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageGermanLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.GERMAN );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "muss eine korrekt formatierte E-Mail-Adresse sein" ),
				violationOf( Range.class ).withMessage( "muss zwischen 18 und 21 sein" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-268")
	public void testEmailAndRangeMessageFrenchLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "doit être une adresse électronique syntaxiquement correcte" ),
				violationOf( Range.class ).withMessage( "doit être compris entre 18 et 21" )
		);
	}

	/**
	 * HV-306. If English is explicitly set as locale for message interpolation, it
	 * must take precedence over the system's default locale.
	 */
	@Test
	@TestForIssue(jiraKey = "HV-306")
	public void testThatExplicitlySetEnglishLocaleHasPrecedenceOverDefaultLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new LocalizedMessageInterpolator( Locale.ENGLISH ) );
		Validator validator = config.buildValidatorFactory().getValidator();
		User user = new User();
		user.setEmail( "foo" );
		user.setAge( 16 );
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "must be a well-formed email address" ),
				violationOf( Range.class ).withMessage( "must be between 18 and 21" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-256")
	public void testConditionalDecimalMinMessageDependingOnInclusiveFlag() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.ENGLISH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();


		Set<ConstraintViolation<DoubleHolder>> constraintViolations = validator.validate( new DoubleHolder() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMin.class ).withMessage( "must be greater than or equal to 1.0" ),
				violationOf( DecimalMin.class ).withMessage( "must be greater than 1.0" ),
				violationOf( DecimalMax.class ).withMessage( "must be less than or equal to 1.0" ),
				violationOf( DecimalMax.class ).withMessage( "must be less than 1.0" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1943")
	public void testConditionalDecimalMinMaxMessagesForFrenchLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();


		Set<ConstraintViolation<DoubleHolder>> constraintViolations = validator.validate( new DoubleHolder() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DecimalMin.class ).withMessage( "doit être supérieur ou égal à 1.0" ),
				violationOf( DecimalMin.class ).withMessage( "doit être supérieur à 1.0" ),
				violationOf( DecimalMax.class ).withMessage( "doit être inférieur ou égal à 1.0" ),
				violationOf( DecimalMax.class ).withMessage( "doit être inférieur à 1.0" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1943")
	public void testConditionalDurationMinMaxMessagesForFrenchLocale() {
		Configuration<?> config = ValidatorUtil.getConfiguration( Locale.FRENCH );
		config.messageInterpolator( new ResourceBundleMessageInterpolator() );
		Validator validator = config.buildValidatorFactory().getValidator();


		Set<ConstraintViolation<DurationHolder>> constraintViolations = validator.validate( new DurationHolder() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMin.class ).withMessage( "doit être plus long ou égal à 2 jours" ),
				violationOf( DurationMin.class ).withMessage( "doit être plus long que 2 jours" ),
				violationOf( DurationMax.class ).withMessage( "doit être plus court ou égal à 2 jours" ),
				violationOf( DurationMax.class ).withMessage( "doit être plus court que 2 jours" )
		);
	}

	private static class DoubleHolder {
		@DecimalMax(value = "1.0")
		private final double inclusiveMaxDouble;
		@DecimalMax(value = "1.0", inclusive = false)
		private final double exclusiveMaxDouble;

		@DecimalMin(value = "1.0")
		private final double inclusiveMinDouble;
		@DecimalMin(value = "1.0", inclusive = false)
		private final double exclusiveMinDouble;

		private DoubleHolder() {
			this.inclusiveMaxDouble = 1.1;
			this.exclusiveMaxDouble = 1.0;

			this.inclusiveMinDouble = 0.9;
			this.exclusiveMinDouble = 1.0;
		}
	}

	private static class DurationHolder {
		@DurationMax(days = 2L)
		private final Duration inclusiveMaxDuration;
		@DurationMax(days = 2L, inclusive = false)
		private final Duration exclusiveMaxDuration;

		@DurationMin(days = 2L)
		private final Duration inclusiveMinDuration;
		@DurationMin(days = 2L, inclusive = false)
		private final Duration exclusiveMinDuration;

		private DurationHolder() {
			this.inclusiveMaxDuration = Duration.ofDays( 3 );
			this.exclusiveMaxDuration = Duration.ofDays( 2 );

			this.inclusiveMinDuration = Duration.ofDays( 1 );
			this.exclusiveMinDuration = Duration.ofDays( 2 );
		}
	}

	/**
	 * A message interpolator that enforces one given locale to be used for message
	 * interpolation.
	 *
	 * @author Gunnar Morling
	 */
	private static class LocalizedMessageInterpolator extends ResourceBundleMessageInterpolator {

		private Locale locale;

		public LocalizedMessageInterpolator(Locale locale) {
			this.locale = locale;
		}

		@Override
		public String interpolate(String messageTemplate, Context context) {
			return interpolate( messageTemplate, context, this.locale );
		}
	}
}
