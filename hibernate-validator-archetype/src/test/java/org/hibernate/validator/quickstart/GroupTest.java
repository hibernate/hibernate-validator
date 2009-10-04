/**
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
package org.hibernate.validator.quickstart;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A module test that shows how to use the grouping functionality of Bean Validation.
 *
 * @author Hardy Ferentschik
 */
public class GroupTest {


	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testDriveAway() {
		// create a car and check that everything is ok with it.
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals( 0, constraintViolations.size() );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validate( car, CarChecks.class );
		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"The car has to pass the vehicle inspection first", constraintViolations.iterator().next().getMessage()
		);

		// let's go to the vehicle inspection
		car.setPassedVehicleInspection( true );
		assertEquals( 0, validator.validate( car ).size() );

		// now let's add a driver. He is 18, but has not passed the driving test yet
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		car.setDriver( john );
		constraintViolations = validator.validate( car, DriverChecks.class );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "You first have to pass the driving test", constraintViolations.iterator().next().getMessage() );

		// ok, John passes the test
		john.passedDrivingTest( true );
		assertEquals( 0, validator.validate( car, DriverChecks.class ).size() );

		// just checking that everything is in order now
		assertEquals( 0, validator.validate( car, Default.class, CarChecks.class, DriverChecks.class ).size() );
	}

	@Test
	public void testOrderedChecks() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		car.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		car.setDriver( john );

		assertEquals( 0, validator.validate( car, OrderedChecks.class ).size() );
	}

	@Test
	public void testOrderedChecksWithRedefinedDefault() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 2 );
		rentalCar.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		rentalCar.setDriver( john );

		assertEquals( 0, validator.validate( rentalCar, Default.class, DriverChecks.class ).size() );
	}
}
