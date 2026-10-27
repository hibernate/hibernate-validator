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

import org.hibernate.validator.constraints.Encoding;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link Encoding} are validated.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class EncodingConstrainedTest extends AbstractConstrainedTest {

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void testValid() {
		Foo foo = new Foo( "Hello, World!" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void testInvalid() {
		Foo foo = new Foo( "café" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Encoding.class ).withMessage( "must only contain characters encodable in US-ASCII" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void testNullIsValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2284")
	public void testEmptyIsValid() {
		Foo foo = new Foo( "" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	private static class Foo {

		@Encoding(charset = "US-ASCII")
		private final String value;

		public Foo(String value) {
			this.value = value;
		}
	}
}
