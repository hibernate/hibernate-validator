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

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class LuhnCheckConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testLuhnCheck() {
		Foo foo = new Foo( "A79927398713" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testLuhnCheckInvalid() {
		Foo foo = new Foo( "A7992739871" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( LuhnCheck.class )
		);
	}

	private static class Foo {

		@LuhnCheck
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
