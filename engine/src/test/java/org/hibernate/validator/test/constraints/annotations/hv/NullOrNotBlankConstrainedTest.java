/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.NullOrNotBlank;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2193")
public class NullOrNotBlankConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void notBlankIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "foobar" ) );
		assertNoViolations( violations );
	}

	@Test
	public void blankIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "   " ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
		);
	}

	@Test
	public void emptyIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
		);
	}

	@Test
	public void optionalWithValueIsValid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( Optional.of( "foobar" ) ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyOptionalIsValid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( Optional.empty() ) );
		assertNoViolations( violations );
	}

	@Test
	public void optionalWithBlankValueIsInvalid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( Optional.of( "   " ) ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
		);
	}

	private static class Foo {

		@NullOrNotBlank
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		private final Optional<@NullOrNotBlank String> string;

		public Bar(Optional<String> string) {
			this.string = string;
		}
	}
}
