package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.MaxLines;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2293")
public class MaxLinesConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testMaxLinesValid() {
		Foo foo = new Foo( "single line" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesTwoLinesValid() {
		Foo foo = new Foo( "line one\nline two" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesExceeded() {
		Foo foo = new Foo( "one\ntwo\nthree" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( MaxLines.class )
		);
	}

	private static class Foo {

		@MaxLines(2)
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
