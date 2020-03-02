/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@SuppressWarnings("deprecation")
public class EmailConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testEmail() {
		Foo foo = new Foo( "foo@example.com" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testEmailInvalid() {
		Foo foo = new Foo( "foo@e@" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class )
		);
	}

	private static class Foo {

		@Email
		private final String email;

		public Foo(String email) {
			this.email = email;
		}
	}
}
