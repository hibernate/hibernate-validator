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

import org.hibernate.validator.constraints.Contains;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Sean Okafor
 */
public class ContainsConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testContainsValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testContainsInvalid() {
		Foo foo = new Foo( "hello" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Contains.class )
		);
	}

	@Test
	public void testContainsNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMinRequiredValid() {
		Bar bar = new Bar( "foo-baz" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertNoViolations( violations );
	}

	@Test
	public void testMinRequiredInvalid() {
		Bar bar = new Bar( "hello" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Contains.class )
		);
	}

	@Test
	public void testIgnoreCaseValid() {
		Baz baz = new Baz( "FOOBAR" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertNoViolations( violations );
	}

	@Test
	public void testIgnoreCaseInvalid() {
		Baz baz = new Baz( "hello" );
		Set<ConstraintViolation<Baz>> violations = validator.validate( baz );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Contains.class )
		);
	}

	private static class Foo {

		@Contains({ "foo", "bar" })
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		@Contains(value = { "foo", "bar", "baz" }, minRequired = 2)
		private final String string;

		public Bar(String string) {
			this.string = string;
		}
	}

	private static class Baz {

		@Contains(value = { "foo", "bar" }, ignoreCase = true)
		private final String string;

		public Baz(String string) {
			this.string = string;
		}
	}
}
