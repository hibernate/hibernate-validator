package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.NormalizedWhitespace;
import org.hibernate.validator.constraints.NormalizedWhitespace.Rule;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2296")
public class NormalizedWhitespaceConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testNormalizedWhitespaceValid() {
		Foo foo = new Foo( "hello world" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testNormalizedWhitespaceNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testNormalizedWhitespaceConsecutiveSpacesInvalid() {
		Foo foo = new Foo( "hello  world" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NormalizedWhitespace.class )
		);
	}

	@Test
	public void testNormalizedWhitespaceTabInvalid() {
		Foo foo = new Foo( "hello\tworld" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NormalizedWhitespace.class )
		);
	}

	@Test
	public void testNoConsecutiveOnlyValid() {
		Bar bar = new Bar( "hello\tworld" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertNoViolations( violations );
	}

	@Test
	public void testNoConsecutiveOnlyInvalid() {
		Bar bar = new Bar( "hello  world" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NormalizedWhitespace.class )
		);
	}

	private static class Foo {

		@NormalizedWhitespace
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		@NormalizedWhitespace(Rule.NO_CONSECUTIVE)
		private final String string;

		public Bar(String string) {
			this.string = string;
		}
	}
}
