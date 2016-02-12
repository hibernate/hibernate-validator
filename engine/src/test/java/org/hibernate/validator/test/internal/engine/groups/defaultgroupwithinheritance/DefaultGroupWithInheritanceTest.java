/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupwithinheritance;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

/**
 * @author Gunnar Morling
 */
public class DefaultGroupWithInheritanceTest {

	@Test
	@TestForIssue(jiraKey = "HV-1055")
	public void testGroupInheritanceWithinRedeclaredGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<A>> violations = validator.validate( new A() );
		assertCorrectPropertyPaths( violations, "foo", "bar" );
	}

	@Test
	public void testGroupInheritanceWithinPassedGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<B>> violations = validator.validate( new B(), Max.class, Min.class );
		assertCorrectPropertyPaths( violations, "foo", "bar" );
	}
}
