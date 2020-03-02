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

import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class EANConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testEANNumber() {
		Foo foo = new Foo( "4006381333931" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testEANInvalid() {
		Foo foo = new Foo( "400638133393" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( EAN.class )
		);
	}

	private static class Foo {

		@EAN
		private final String number;

		public Foo(String number) {
			this.number = number;
		}
	}
}
