/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
import org.hibernate.validator.cfg.defs.DurationMaxDef;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationMaxValidatorTest {

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

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testMessage() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.field( "timeToComplete" )
				.constraint( new DurationMaxDef()
						.days( 1 )
						.nanos( 100 )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 2 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( DurationMax.class ).withMessage( "must be shorter than or equal to 1 day 100 nanos" )
		);
	}

	private void doTesting(boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMax.class );
		descriptorBuilder.setAttribute( "nanos", 100L );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMax annotation = descriptorBuilder.build().getAnnotation();

		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( Duration.ofNanos( 10L ), null ) );
		Assert.assertEquals( validator.isValid( Duration.ofNanos( 100L ), null ), inclusive );
		Assert.assertFalse( validator.isValid( Duration.ofNanos( 101L ), null ) );
		Assert.assertFalse( validator.isValid( Duration.ofSeconds( 100L ), null ) );
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
