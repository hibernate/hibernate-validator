/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintTest {

	@Test
	public void testRangeConstraint() {
		Validator validator = ValidatorUtil.getValidator();

		Elevator elevator = new Elevator();
		elevator.setCurrentFloor( -3 );
		Set<ConstraintViolation<Elevator>> constraintViolations = validator.validate( elevator );

		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation( constraintViolations.iterator().next(), "Invalid floor" );

		elevator.setCurrentFloor( -2 );
		constraintViolations = validator.validate( elevator );

		assertNumberOfViolations( constraintViolations, 0 );

		elevator.setCurrentFloor( 45 );
		constraintViolations = validator.validate( elevator );

		assertNumberOfViolations( constraintViolations, 0 );

		elevator.setCurrentFloor( 50 );
		constraintViolations = validator.validate( elevator );

		assertNumberOfViolations( constraintViolations, 0 );

		elevator.setCurrentFloor( 51 );
		constraintViolations = validator.validate( elevator );

		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation( constraintViolations.iterator().next(), "Invalid floor" );
	}
}
