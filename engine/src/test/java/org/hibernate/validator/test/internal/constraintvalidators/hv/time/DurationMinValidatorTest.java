/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DurationMinDef;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationMinValidatorTest {

	private static Locale PREVIOUS_LOCALE;

	@BeforeClass
	public static void saveLocale() {
		PREVIOUS_LOCALE = Locale.getDefault();
	}

	@AfterClass
	public static void restoreLocale() {
		Locale.setDefault( PREVIOUS_LOCALE );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testIsValid() {
		doTesting( true );
		doTesting( false );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testWithValidator() {
		Validator validator = getValidator();

		assertNoViolations( validator.validate( new Task( null ) ) );
		assertNoViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ) );
		assertThat( validator.validate( new Task( Duration.ofSeconds( 9 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMin.class )
				);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.field( "timeToComplete" )
				.constraint( new DurationMinDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 1 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMin.class )
		);

		task = new AnotherTask( Duration.ofDays( 2 ) );
		constraintViolations = validator.validate( task );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testMessage() {
		testMessage( "must be longer than or equal to 30 days 12 hours 50 minutes", true, 30, 12, 50, Duration.ofDays( 2 ) );
		testMessage( "must be longer than 30 days 12 hours 50 minutes", false, 30, 12, 50, Duration.ofDays( 2 ) );
		testMessage( "must be longer than or equal to 0", true, 0, 0, 0, Duration.ofDays( -2 ) );
		testMessage( "must be longer than 0", false, 0, 0, 0, Duration.ofDays( -2 ) );
	}

	public void testMessage(String message, boolean inclusive, int days, int hours, int minutes, Duration value) {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.field( "timeToComplete" )
				.constraint( new DurationMinDef()
						.days( days )
						.hours( hours )
						.minutes( minutes )
						.inclusive( inclusive )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( value );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMin.class ).withMessage( message )
		);
	}

	private void doTesting(boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMin.class );
		descriptorBuilder.setAttribute( "nanos", 100L );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMin annotation = descriptorBuilder.build().getAnnotation();

		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( Duration.ofNanos( 1000L ), null ) );
		Assert.assertEquals( validator.isValid( Duration.ofNanos( 100L ), null ), inclusive );
		Assert.assertTrue( validator.isValid( Duration.ofSeconds( 100L ), null ) );
		Assert.assertFalse( validator.isValid( Duration.ofNanos( 10L ), null ) );
	}

	private static class Task {

		@DurationMin(seconds = 10)
		private final Duration timeToComplete;

		public Task(Duration timeToComplete) {
			this.timeToComplete = timeToComplete;
		}
	}

	private static class AnotherTask {

		private final Duration timeToComplete;

		public AnotherTask(Duration timeToComplete) {
			this.timeToComplete = timeToComplete;
		}
	}
}
