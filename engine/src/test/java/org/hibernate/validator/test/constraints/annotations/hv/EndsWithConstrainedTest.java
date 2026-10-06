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

import org.hibernate.validator.constraints.EndsWith;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Andrea Boriero
 */
@Test
public class EndsWithConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testEndsWithValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testEndsWithInvalid() {
		Foo foo = new Foo( "hello" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( EndsWith.class )
		);
	}

	@Test
	public void testEndsWithNullValid() {
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
		Bar bar = new Bar( "hello" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertThat( violations ).containsOnlyViolations(
				violationOf( EndsWith.class )
		);
	}

	@Test
	public void testMultipleSuffixesValid() {
		Baz baz = new Baz( "foobar" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertNoViolations( violations );
	}

	@Test
	public void testMultipleSuffixesInvalid() {
		Baz baz = new Baz( "hello" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertThat( violations ).containsOnlyViolations(
				violationOf( EndsWith.class )
		);
	}

	private static class Foo {

		@EndsWith("bar")
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		@EndsWith(value = "bar", ignoreCase = true)
		private final String string;

		public Bar(String string) {
			this.string = string;
		}
	}

	private static class Baz {

		@EndsWith({ "foo", "bar" })
		private final String string;

		public Baz(String string) {
			this.string = string;
		}
	}
}
