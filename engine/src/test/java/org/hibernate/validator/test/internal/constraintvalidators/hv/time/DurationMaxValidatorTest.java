/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.time;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.Duration;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DurationMaxDef;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationMaxValidatorTest {

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
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 1 ) ) ), 0 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ), 1 );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1232")
	public void testProgrammaticConstraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( AnotherTask.class )
				.property( "timeToComplete", FIELD )
				.constraint( new DurationMaxDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		AnotherTask task = new AnotherTask( Duration.ofDays( 2 ) );
		Set<ConstraintViolation<AnotherTask>> constraintViolations = validator.validate( task );
		assertCorrectConstraintTypes( constraintViolations, DurationMax.class );

		task = new AnotherTask( Duration.ofDays( 1 ) );
		constraintViolations = validator.validate( task );
		assertNumberOfViolations( constraintViolations, 0 );

	}

	private void doTesting(boolean inclusive) {
		AnnotationDescriptor<DurationMax> descriptor = new AnnotationDescriptor<>( DurationMax.class );
		descriptor.setValue( "nanos", 100L );
		descriptor.setValue( "inclusive", inclusive );
		DurationMax annotation = AnnotationFactory.create( descriptor );

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
		private Duration timeToComplete;

		public Task(Duration timeToComplete) {
			this.timeToComplete = timeToComplete;
		}
	}

	private static class AnotherTask {

		private Duration timeToComplete;

		public AnotherTask(Duration timeToComplete) {
			this.timeToComplete = timeToComplete;
		}
	}

}
