/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.inheritance;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class GroupInheritanceTest {

	/**
	 * HV-288, HV-1057.
	 */
	@Test
	public void testGroupInheritanceWithinGroupSequence() {
		Validator validator = ValidatorUtil.getValidator();
		Try tryMe = new Try();
		tryMe.field3 = "bar";

		Set<ConstraintViolation<Try>> violations = validator.validate( tryMe, Try.GlobalCheck.class );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "field1" )
						.withMessage( "field1" ),
				violationOf( NotNull.class )
						.withProperty( "field2" )
						.withMessage( "field2" )
		);
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
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withProperty( "field1" )
						.withMessage( "field1" ),
				violationOf( NotNull.class )
						.withProperty( "field2" )
						.withMessage( "field2" )
		);
	}
}
