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

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@SuppressWarnings("deprecation")
public class NotBlankConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testNotBlank() {
		Foo foo = new Foo( "foo" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testNotBlankInvalid() {
		Foo foo = new Foo( "" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);
	}

	private static class Foo {

		@NotBlank
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
