/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.time.Duration;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testDuration() {
		Foo foo = new Foo( Duration.ofHours( 1 ) );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testDurationInvalid() {
		Foo foo = new Foo( Duration.ofDays( 5 ) );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( DurationMax.class ).withProperty( "duration" )
		);

		foo = new Foo( Duration.ofNanos( 5 ) );
		violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( DurationMin.class ).withProperty( "duration" )
		);
	}

	private static class Foo {

		@DurationMax(days = 1)
		@DurationMin(seconds = 1)
		private final Duration duration;

		public Foo(Duration duration) {
			this.duration = duration;
		}
	}
}
