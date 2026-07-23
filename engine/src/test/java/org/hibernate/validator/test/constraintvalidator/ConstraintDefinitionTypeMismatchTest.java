/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraintvalidator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1592")
public class ConstraintDefinitionTypeMismatchTest {

	private Validator validator;

	@BeforeEach
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void constraint_validator_constraint_type_mismatch_causes_exception() {
		assertThatThrownBy( () -> validator.validate( new TypeMismatchBean() ) )
				.isInstanceOf( ConstraintDefinitionException.class )
				.hasMessageMatching( "^HV000243:.*" );
	}

	public class TypeMismatchBean {

		@TypeMismatchConstraint
		private String property;
	}
}
