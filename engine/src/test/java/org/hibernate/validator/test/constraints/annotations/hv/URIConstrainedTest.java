/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URI;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Tests for {@link URI} constraint.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2254")
public class URIConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testValidURI() {
		Foo foo = new Foo( "http://example.com" );
		assertNoViolations( validator.validate( foo ) );
	}

	@Test
	public void testInvalidURI() {
		Foo foo = new Foo( "ht tp://invalid uri" );
		assertThat( validator.validate( foo ) ).containsOnlyViolations(
				violationOf( URI.class )
		);
	}

	@Test
	public void testValidAbsoluteURI() {
		Foo foo = new Foo( "https://www.example.com/path?query=value#fragment" );
		assertNoViolations( validator.validate( foo ) );
	}

	@Test
	public void testValidOpaqueURI() {
		Foo foo = new Foo( "mailto:user@example.com" );
		assertNoViolations( validator.validate( foo ) );
	}

	@Test
	public void testValidRelativeURI() {
		Foo foo = new Foo( "../relative/path" );
		assertNoViolations( validator.validate( foo ) );
	}

	@Test
	public void testValidIPv6URI() {
		Foo foo = new Foo( "http://[2001:db8::1]/" );
		assertNoViolations( validator.validate( foo ) );
	}

	@Test
	public void testNullIsValid() {
		Foo foo = new Foo( null );
		assertThat( validator.validate( foo ) ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	public void testEmptyStringIsValid() {
		Foo foo = new Foo( "" );
		assertNoViolations( validator.validate( foo ) );
	}

	private static class Foo {
		@NotNull
		@URI
		private final String uri;

		public Foo(String uri) {
			this.uri = uri;
		}
	}
}
