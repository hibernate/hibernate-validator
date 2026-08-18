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

import org.hibernate.validator.constraints.IBAN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link IBAN} are validated.
 *
 * @author Andrea Boriero
 */
public class IBANConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testIBAN() {
		Foo foo = new Foo( "GB82WEST12345698765432" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testIBANInvalid() {
		Foo foo = new Foo( "GB94WEST12345698765432" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( IBAN.class ).withMessage( "invalid International Bank Account Number (IBAN)" )
		);
	}

	private static class Foo {

		@IBAN
		private final String number;

		public Foo(String number) {
			this.number = number;
		}
	}
}
