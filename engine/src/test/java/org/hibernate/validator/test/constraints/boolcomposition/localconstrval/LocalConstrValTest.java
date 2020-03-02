/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class LocalConstrValTest {

	/**
	 * HV-390
	 * Used to test whether boolean composition works with local ConstraintValidators
	 */
	@Test
	public void testCorrectBooleanEvaluation() {
		Validator currentValidator = ValidatorUtil.getValidator();

		//nothing should fail, the pattern matches on name
		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "6chars", "WWWW" )
		);

		assertNoViolations( constraintViolations );

		//nickname is too long
		constraintViolations = currentValidator.validate(
				new Person(
						"12characters", "loongstring"
				)
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( SmallString.class ).withProperty( "nickName" )
		);

		//nickName fails for violating @Size, but is reported as SingleViolation
		//name fails for violating both Pattern and the test in LongStringValidator. In a way it is reported
		//both as single violation and as multiple violations (weird).
		constraintViolations = currentValidator.validate(
				new Person(
						"exactlyTEN", "tinystr"
				)
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( SmallString.class ).withProperty( "nickName" ),
				violationOf( Pattern.class ).withProperty( "name" ),
				violationOf( PatternOrLong.class ).withProperty( "name" )
		);
	}
}
