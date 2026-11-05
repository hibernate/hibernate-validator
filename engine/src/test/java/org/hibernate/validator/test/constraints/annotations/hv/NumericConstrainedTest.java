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

import org.hibernate.validator.constraints.Numeric;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2285")
public class NumericConstrainedTest extends AbstractConstrainedTest {

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
	public void digitsOnlyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "0123456789" ) );
		assertNoViolations( violations );
	}

	@Test
	public void nonDigitIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "12.5" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Numeric.class )
		);
	}

	private static class Foo {

		@Numeric
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
