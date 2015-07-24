/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.inheritance;

import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Hardy Ferentschik
 */
public class GroupInheritanceTest {

	/**
	 * HV-288
	 */
	@Test
	public void testGroupInheritanceWithinGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();
		Try tryMe = new Try();
		tryMe.field2 = "foo";
		tryMe.field3 = "bar";

		Set<ConstraintViolation<Try>> violations = validator.validate( tryMe, Try.GlobalCheck.class );
		assertCorrectConstraintViolationMessages( violations, "field1" );
	}

	/**
	 * HV-353
	 */
	@Test
	public void testGroupInheritance() {
		Validator validator = ValidatorUtil.getValidator();
		Try tryMe = new Try();
		tryMe.field3 = "foo";

		Set<ConstraintViolation<Try>> violations = validator.validate( tryMe, Try.Component.class );
		assertNumberOfViolations( violations, 2 );
		assertCorrectConstraintViolationMessages( violations, "field1", "field2" );
	}
}
