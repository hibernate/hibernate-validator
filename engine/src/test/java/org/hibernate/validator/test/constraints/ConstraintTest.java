/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
