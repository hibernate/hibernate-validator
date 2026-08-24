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

import org.hibernate.validator.constraints.StartsWith;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Koen Aers
 */
public class StartsWithConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testStartsWithValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testStartsWithInvalid() {
		Foo foo = new Foo( "barfoo" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( StartsWith.class )
		);
	}

	@Test
	public void testStartsWithNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testIgnoreCaseValid() {
		Bar bar = new Bar( "FOOBAR" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertNoViolations( violations );
	}

	@Test
	public void testIgnoreCaseInvalid() {
		Bar bar = new Bar( "barfoo" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertThat( violations ).containsOnlyViolations(
				violationOf( StartsWith.class )
		);
	}

	@Test
	public void testMultipleValuesValid() {
		Baz baz = new Baz( "fubar" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertNoViolations( violations );
	}

	@Test
	public void testMultipleValuesInvalid() {
		Baz baz = new Baz( "barfoo" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertThat( violations ).containsOnlyViolations(
				violationOf( StartsWith.class )
		);
	}

	private static class Foo {

		@StartsWith("foo")
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		@StartsWith(value = "foo", ignoreCase = true)
		private final String string;

		public Bar(String string) {
			this.string = string;
		}
	}

	private static class Baz {

		@StartsWith({ "foo", "fu" })
		private final String string;

		public Baz(String string) {
			this.string = string;
		}
	}
}
