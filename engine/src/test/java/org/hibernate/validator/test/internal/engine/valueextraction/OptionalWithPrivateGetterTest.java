/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-975")
public class OptionalWithPrivateGetterTest {

	@Test
	public void testConstraintCanBePlacedOnPrivateGetter() {
		Validator validator = getValidator();

		Set<ConstraintViolation<Project>> constraintViolations = validator.validate(
				new Project( LocalDate.MIN, LocalDate.MAX )
		);
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testConstraintOnPrivateGetterReturnsConstraintViolation() {
		Validator validator = getValidator();

		LocalDate[][] testData = { { LocalDate.MAX, LocalDate.MIN }, { LocalDate.now(), null }, { null, null } };

		for ( LocalDate[] dates : testData ) {
			Set<ConstraintViolation<Project>> constraintViolations = validator.validate(
					new Project( dates[0], dates[1] )
			);
			assertThat( constraintViolations ).containsOnlyViolations(
					violationOf( AssertTrue.class ).withProperty( "startBeforeEnd" )
			);
		}
	}
}
