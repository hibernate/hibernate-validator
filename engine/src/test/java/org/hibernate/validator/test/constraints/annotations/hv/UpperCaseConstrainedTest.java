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

import org.hibernate.validator.constraints.UpperCase;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2258")
public class UpperCaseConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "" ) );
		assertNoViolations( violations );
	}

	@Test
	public void uppercaseIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "HELLO" ) );
		assertNoViolations( violations );
	}

	@Test
	public void uppercaseWithDigitsIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "HELLO123" ) );
		assertNoViolations( violations );
	}

	@Test
	public void lowercaseIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "hello" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( UpperCase.class )
		);
	}

	@Test
	public void mixedCaseIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "Hello" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( UpperCase.class )
		);
	}

	private static class Foo {

		@UpperCase
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
