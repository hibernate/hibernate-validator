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

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ScriptAssertConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testScriptAssertNumber() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( true ) );
		assertNoViolations( violations );
	}

	@Test
	public void testScriptAssertInvalid() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo( false ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( ScriptAssert.class )
		);
	}

	@ScriptAssert(lang = "groovy", script = "_this.test")
	private static class Foo {

		@SuppressWarnings("unused")
		private final boolean test;

		public Foo(boolean test) {
			this.test = test;
		}
	}
}
