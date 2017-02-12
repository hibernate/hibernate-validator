/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.Duration;
import javax.validation.Validator;

import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationMinValidatorTest {

	@Test
	@TestForIssue( jiraKey = "HV-1232" )
	public void testIsValid() {
		doTesting( true );
		doTesting( false );
	}

	@Test
	@TestForIssue( jiraKey = "HV-1232" )
	public void testWithValidator() {
		Validator validator = getValidator();

		assertNumberOfViolations( validator.validate( new Task( null ) ), 0 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 9 ) ) ), 1 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ), 0 );
		assertNumberOfViolations( validator.validate( new Task( Duration.ofSeconds( 11 ) ) ), 0 );

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
		private Duration timeToComplete;

		public Task(Duration timeToComplete) {
			this.timeToComplete = timeToComplete;
		}
	}

}
