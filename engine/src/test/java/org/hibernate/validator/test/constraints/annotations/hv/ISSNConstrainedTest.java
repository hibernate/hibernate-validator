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

import org.hibernate.validator.constraints.ISSN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link ISSN} are validated.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2252")
public class ISSNConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testISSN() {
		// Using ISSN-13 (default type)
		Foo foo = new Foo( "9770378595002" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testISSNInvalid() {
		// Invalid ISSN-13 (wrong check digit)
		Foo foo = new Foo( "9770378595003" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( ISSN.class ).withMessage( "invalid ISSN" )
		);
	}

	private static class Foo {

		@ISSN
		private final String number;

		public Foo(String number) {
			this.number = number;
		}
	}
}
