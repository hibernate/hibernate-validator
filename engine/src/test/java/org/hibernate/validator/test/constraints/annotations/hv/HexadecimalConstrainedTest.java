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

import org.hibernate.validator.constraints.Hexadecimal;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link Hexadecimal} are validated.
 *
 * @since 9.2
 */
public class HexadecimalConstrainedTest extends AbstractConstrainedTest {

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void testValid() {
		Foo foo = new Foo( "deadBEEF" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void testInvalid() {
		Foo foo = new Foo( "deadBEEG" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Hexadecimal.class ).withMessage( "must be a valid hexadecimal string" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void testNullIsValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void testEmptyIsInvalid() {
		Foo foo = new Foo( "" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Hexadecimal.class ).withMessage( "must be a valid hexadecimal string" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2250")
	public void testPrefix() {
		Bar bar = new Bar( "0x1A" );
		Set<ConstraintViolation<Bar>> violations = validator.validate( bar );
		assertNoViolations( violations );

		Bar barInvalid = new Bar( "1A" );
		violations = validator.validate( barInvalid );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Hexadecimal.class ).withMessage( "must be a valid hexadecimal string" )
		);
	}

	private static class Foo {

		@Hexadecimal
		private final String value;

		public Foo(String value) {
			this.value = value;
		}
	}

	private static class Bar {

		@Hexadecimal(prefix = "0x")
		private final String value;

		public Bar(String value) {
			this.value = value;
		}
	}
}
