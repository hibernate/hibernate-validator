package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Base64;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2289")
public class Base64ConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null, null ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "", "" ) );
		assertNoViolations( violations );
	}

	@Test
	public void validEncodingIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "+/+/", "-_-_" ) );
		assertNoViolations( violations );
	}

	@Test
	public void invalidEncodingIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "-_-_", "+/+/" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Base64.class ).withPropertyPath( pathWith().property( "standard" ) ),
				violationOf( Base64.class ).withPropertyPath( pathWith().property( "urlSafe" ) )
		);
	}

	private static class Foo {

		@Base64
		private final String standard;

		@Base64(urlSafe = true)
		private final String urlSafe;

		public Foo(String standard, String urlSafe) {
			this.standard = standard;
			this.urlSafe = urlSafe;
		}
	}
}
