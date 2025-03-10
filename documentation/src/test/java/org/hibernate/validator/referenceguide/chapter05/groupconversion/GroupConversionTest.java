/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter05.groupconversion;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class GroupConversionTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validateDriverChecksTogetherWithCarChecks() {
		//tag::validateDriverChecksTogetherWithCarChecks[]
		// create a car and validate. The Driver is still null and does not get validated
		Car car = new Car( "VW", "USD-123", 4 );
		car.setPassedVehicleInspection( true );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals( 0, constraintViolations.size() );

		// create a driver who has not passed the driving test
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );

		// now let's add a driver to the car
		car.setDriver( john );
		constraintViolations = validator.validate( car );
		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"The driver constraint should also be validated as part of the default group",
				constraintViolations.iterator().next().getMessage(),
				"You first have to pass the driving test"
		);
		//end::validateDriverChecksTogetherWithCarChecks[]
	}
}
