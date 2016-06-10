/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

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

		assertNumberOfViolations( constraintViolations, 0 );

		//nickname is too long
		constraintViolations = currentValidator.validate(
				new Person(
						"12characters", "loongstring"
				)
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, SmallString.class );
		assertCorrectPropertyPaths( constraintViolations, "nickName" );

		//nickName fails for violating @Size, but is reported as SingleViolation
		//name fails for violating both Pattern and the test in LongStringValidator. In a way it is reported 
		//both as single violation and as multiple violations (weird).
		constraintViolations = currentValidator.validate(
				new Person(
						"exactlyTEN", "tinystr"
				)
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectConstraintTypes( constraintViolations, SmallString.class, Pattern.class, PatternOrLong.class );
		assertCorrectPropertyPaths( constraintViolations, "nickName", "name", "name" );
	}
}
