package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Odd;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2301")
public class OddConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void oddIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 7 ) );
		assertNoViolations( violations );
	}

	@Test
	public void evenIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 4 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Odd.class )
		);
	}

	@Test
	public void zeroIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 0 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Odd.class )
		);
	}

	private static class Foo {

		@Odd
		private final Integer value;

		public Foo(Integer value) {
			this.value = value;
		}
	}
}
