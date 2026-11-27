package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Even;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2301")
public class EvenConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void evenIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 42 ) );
		assertNoViolations( violations );
	}

	@Test
	public void zeroIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 0 ) );
		assertNoViolations( violations );
	}

	@Test
	public void oddIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 3 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Even.class )
		);
	}

	private static class Foo {

		@Even
		private final Integer value;

		public Foo(Integer value) {
			this.value = value;
		}
	}
}
