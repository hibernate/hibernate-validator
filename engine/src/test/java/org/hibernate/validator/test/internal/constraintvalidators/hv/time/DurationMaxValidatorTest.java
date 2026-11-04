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
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DurationMaxDef;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Marko Bekhta
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DurationMaxValidatorTest {

	private static Locale PREVIOUS_LOCALE;

	@BeforeAll
	public static void saveLocale() {
		PREVIOUS_LOCALE = Locale.getDefault();
	}

	@AfterAll
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
		assertNoViolations( validator.validate( new Task( Duration.ofSeconds( 1 ) ) ) );
		assertThat( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMax.class )
				);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.field( "timeToComplete" )
				.constraint( new DurationMaxDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 2 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMax.class )
		);

		task = new AnotherTask( Duration.ofDays( 1 ) );
		constraintViolations = validator.validate( task );
		assertNoViolations( constraintViolations );
	}

	@ParameterizedTest
	@MethodSource("testMessageData")
	@TestForIssue(jiraKey = "HV-1232")
	public void testMessage(String message, boolean inclusive, int days, int nanos, Duration value) {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.field( "timeToComplete" )
				.constraint( new DurationMaxDef()
						.days( days )
						.nanos( nanos )
						.inclusive( inclusive )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( value );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMax.class ).withMessage( message )
		);
	}

	private static Stream<Arguments> testMessageData() {
		return Stream.of(
				Arguments.of( "must be shorter than or equal to 1 day 100 nanos", true, 1, 100, Duration.ofDays( 2 ) ),
				Arguments.of( "must be shorter than 1 day 100 nanos", false, 1, 100, Duration.ofDays( 2 ) ),
				Arguments.of( "must be shorter than or equal to 0", true, 0, 0, Duration.ofDays( 2 ) ),
				Arguments.of( "must be shorter than 0", false, 0, 0, Duration.ofDays( 2 ) )
		);
	}

	private void doTesting(boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMax.class );
		descriptorBuilder.setAttribute( "nanos", 100L );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMax annotation = descriptorBuilder.build().getAnnotation();

		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );

		Assertions.assertTrue( validator.isValid( null, null ) );
		Assertions.assertTrue( validator.isValid( Duration.ofNanos( 10L ), null ) );
		Assertions.assertEquals( inclusive, validator.isValid( Duration.ofNanos( 100L ), null ) );
		Assertions.assertFalse( validator.isValid( Duration.ofNanos( 101L ), null ) );
		Assertions.assertFalse( validator.isValid( Duration.ofSeconds( 100L ), null ) );
	}

	private static class Task {

		@DurationMax(seconds = 10)
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
