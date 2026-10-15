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

import org.hibernate.validator.constraints.Alphabetic;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2261")
public class AlphabeticConstrainedTest extends AbstractConstrainedTest {

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
	public void lettersOnlyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "HelloWorld" ) );
		assertNoViolations( violations );
	}

	@Test
	public void digitsAreInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "Hello123" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Alphabetic.class )
		);
	}

	@Test
	public void whitespaceIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "Hello World" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Alphabetic.class )
		);
	}

	private static class Foo {

		@Alphabetic
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
