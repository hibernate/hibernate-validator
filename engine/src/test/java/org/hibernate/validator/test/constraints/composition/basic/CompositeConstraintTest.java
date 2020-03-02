/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.basic;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Gerhard Petracek
 * @author Hardy Ferentschik
 */
public class CompositeConstraintTest {
	@Test
	@TestForIssue(jiraKey = "HV-182")
	public void testCorrectAnnotationTypeForWithReportAsSingleViolation() {

		Validator currentValidator = ValidatorUtil.getValidator();

		for ( int i = 0; i < 100; i++ ) {
			Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
					new Person(
							null, "Gerhard"
					)
			);

			assertThat( constraintViolations ).containsOnlyViolations(
					violationOf( ValidNameSingleViolation.class ).withMessage( "invalid name" )
			);

			constraintViolations = currentValidator.validate(
					new Person(
							"G", "Gerhard"
					)
			);
			assertThat( constraintViolations ).containsOnlyViolations(
					violationOf( ValidNameSingleViolation.class ).withMessage( "invalid name" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-182")
	public void testCorrectAnnotationTypeReportMultipleViolations() {

		Validator currentValidator = ValidatorUtil.getValidator();

		for ( int i = 0; i < 100; i++ ) {
			Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
					new Person(
							"Gerd", null
					)
			);

			assertThat( constraintViolations ).containsOnlyViolations(
					violationOf( NotNull.class ).withMessage( "must not be null" )
			);

			constraintViolations = currentValidator.validate(
					new Person(
							"Gerd", "G"
					)
			);
			assertThat( constraintViolations ).containsOnlyViolations(
					violationOf( Size.class ).withMessage( "size must be between 2 and 10" )
			);
		}
	}
}

