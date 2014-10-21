/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;

/**
 * Test related to the identity of {@link org.hibernate.validator.internal.engine.ConstraintViolationImpl}s.
 *
 * @author Gunnar Morling
 */
public class ConstraintViolationImplIdentityTest {

	Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-665")
	public void testTwoViolationsForDifferentConstraintsAreNotEqual() {
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );

		assertCorrectConstraintTypes( violations, Size.class, DecimalMin.class );
	}

	private static class Foo {
		@Size(min = 2, message = "must be 2 at least")
		@DecimalMin(value = "2", message = "must be 2 at least")
		String name = "1";
	}
}
