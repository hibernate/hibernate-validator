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

import org.hibernate.validator.constraints.Alphanumeric;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2269")
public class AlphanumericConstrainedTest extends AbstractConstrainedTest {

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
	public void lettersAndDigitsOnlyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "HelloWorld123" ) );
		assertNoViolations( violations );
	}

	@Test
	public void whitespaceIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "Hello World" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Alphanumeric.class )
		);
	}

	private static class Foo {

		@Alphanumeric
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
