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

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ParameterScriptAssertConstrainedTest extends AbstractConstrainedTest {

	private ExecutableValidator executableValidator;
	private Foo foo;
	private Method method;

	@Override @BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		executableValidator = validator.forExecutables();
		foo = new Foo();
		method = Foo.class.getDeclaredMethod( "doTest", boolean.class );
	}

	@Test
	public void testParameterScriptAssertNumber() {
		Set<ConstraintViolation<Foo>> violations = executableValidator.validateParameters( foo, method, new Object[] { true } );
		assertNoViolations( violations );
	}

	@Test
	public void testParameterScriptAssertInvalid() {
		Set<ConstraintViolation<Foo>> violations = executableValidator.validateParameters( foo, method, new Object[] { false } );
		assertThat( violations ).containsOnlyViolations(
				violationOf( ParameterScriptAssert.class )
		);
	}

	private static class Foo {

		@ParameterScriptAssert(lang = "groovy", script = "test")
		public boolean doTest(boolean test) {
			return test;
		}
	}
}
