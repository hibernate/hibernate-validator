/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Trimmed;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2248")
public class TrimmedConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testTrimmedValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testTrimmedNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testTrimmedLeadingWhitespaceInvalid() {
		Foo foo = new Foo( " foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Trimmed.class )
		);
	}

	@Test
	public void testTrimmedTrailingWhitespaceInvalid() {
		Foo foo = new Foo( "foobar " );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Trimmed.class )
		);
	}

	private static class Foo {

		@Trimmed
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
