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

import org.hibernate.validator.constraints.BIC;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link BIC} are validated.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2249")
public class BICConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testBIC() {
		Foo foo = new Foo( "DEUTDEFF" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testBICInvalid() {
		Foo foo = new Foo( "DEUTDE" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( BIC.class ).withMessage( "invalid Bank Identifier Code (BIC)" )
		);
	}

	private static class Foo {

		@BIC
		private final String code;

		public Foo(String code) {
			this.code = code;
		}
	}
}
