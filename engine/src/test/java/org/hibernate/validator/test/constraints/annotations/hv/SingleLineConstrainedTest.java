package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.SingleLine;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2292")
public class SingleLineConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testSingleLineValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testSingleLineNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testSingleLineWithLineFeedInvalid() {
		Foo foo = new Foo( "foo\nbar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SingleLine.class )
		);
	}

	@Test
	public void testSingleLineWithCarriageReturnInvalid() {
		Foo foo = new Foo( "foo\rbar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SingleLine.class )
		);
	}

	private static class Foo {

		@SingleLine
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
