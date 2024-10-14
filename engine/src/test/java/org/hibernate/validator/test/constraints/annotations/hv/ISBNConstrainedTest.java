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

import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link ISBN} are validated.
 *
 * @author Marko Bekhta
 */
public class ISBNConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testISBN() {
		Foo foo = new Foo( "978-1-56619-909-4" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testISBNInvalid() {
		Foo foo = new Foo( "5412-3456-7890" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( ISBN.class ).withMessage( "invalid ISBN" )
		);
	}

	private static class Foo {

		@ISBN
		private final String number;

		public Foo(String number) {
			this.number = number;
		}
	}
}
