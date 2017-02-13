/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.time;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DurationMinDef;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
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

		assertNumberOfViolations( validator.validate( new Task( null ) ), 0 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 9 ) ) ), 1 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ), 0 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ), 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.property( "timeToComplete", FIELD )
				.constraint( new DurationMinDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 1 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertCorrectConstraintTypes( constraintViolations, DurationMin.class );

		task = new AnotherTask( Duration.ofDays( 2 ) );
		constraintViolations = validator.validate( task );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testMessage() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.property( "timeToComplete", FIELD )
				.constraint( new DurationMinDef()
						.days( 30 )
						.hours( 12 )
						.minutes( 50 )
						.inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 2 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertCorrectConstraintViolationMessages( constraintViolations, "must be longer than 30 days 12 hours 50 minutes" );
	}

	private void doTesting(boolean inclusive) {
		AnnotationDescriptor<DurationMin> descriptor = new AnnotationDescriptor<>( DurationMin.class );
		descriptor.setValue( "nanos", 100L );
		descriptor.setValue( "inclusive", inclusive );
		DurationMin annotation = AnnotationFactory.create( descriptor );

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
