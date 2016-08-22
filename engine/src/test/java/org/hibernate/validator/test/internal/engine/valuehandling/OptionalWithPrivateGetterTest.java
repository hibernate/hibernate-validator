/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.time.LocalDate;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

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
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testConstraintOnPrivateGetterReturnsConstraintViolation() {
		Validator validator = getValidator();

		LocalDate[][] testData = { { LocalDate.MAX, LocalDate.MIN }, { LocalDate.now(), null }, { null, null } };

		for ( LocalDate[] dates : testData ) {
			Set<ConstraintViolation<Project>> constraintViolations = validator.validate(
					new Project( dates[0], dates[1] )
			);
			assertNumberOfViolations( constraintViolations, 1 );
			assertCorrectConstraintTypes( constraintViolations, AssertTrue.class );
			assertCorrectPropertyPaths( constraintViolations, "startBeforeEnd" );
		}
	}
}


