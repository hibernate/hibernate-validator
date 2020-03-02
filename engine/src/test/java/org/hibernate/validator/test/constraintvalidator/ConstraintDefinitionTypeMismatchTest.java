/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1592")
public class ConstraintDefinitionTypeMismatchTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test(expectedExceptions = ConstraintDefinitionException.class, expectedExceptionsMessageRegExp = "^HV000243:.*")
	public void constraint_validator_constraint_type_mismatch_causes_exception() {
		validator.validate( new TypeMismatchBean() );
	}

	public class TypeMismatchBean {

		@TypeMismatchConstraint
		private String property;
	}
}
