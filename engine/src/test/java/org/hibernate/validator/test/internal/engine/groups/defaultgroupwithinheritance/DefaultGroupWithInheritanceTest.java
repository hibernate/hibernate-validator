/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupwithinheritance;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class DefaultGroupWithInheritanceTest {

	@Test
	@TestForIssue(jiraKey = "HV-1055")
	public void testGroupInheritanceWithinRedeclaredGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<A>> violations = validator.validate( new A() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "foo" ),
				violationOf( NotNull.class ).withProperty( "bar" )
		);
	}

	@Test
	public void testGroupInheritanceWithinPassedGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<B>> violations = validator.validate( new B(), Max.class, Min.class );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "foo" ),
				violationOf( NotNull.class ).withProperty( "bar" )
		);
	}
}
