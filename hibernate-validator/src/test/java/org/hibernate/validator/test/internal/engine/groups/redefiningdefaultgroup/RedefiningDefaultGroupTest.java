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
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupSequence;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class RedefiningDefaultGroupTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		validator = getValidator();
	}

	@Test
	public void testDriveAway() {
		// create a car and check that everything is ok with it.
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertNumberOfViolations( constraintViolations, 0 );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validate( car, CarChecks.class );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"The car has to pass the vehicle inspection first"
		);

		// let's go to the vehicle inspection
		car.setPassedVehicleInspection( true );
		constraintViolations = validator.validate( car );
		assertNumberOfViolations( constraintViolations, 0 );

		// now let's add a driver. He is 18, but has not passed the driving test yet
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		car.setDriver( john );
		constraintViolations = validator.validate( car, DriverChecks.class );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"You first have to pass the driving test"
		);

		// ok, John passes the test
		john.passedDrivingTest( true );
		constraintViolations = validator.validate( car, DriverChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );

		// just checking that everything is in order now
		constraintViolations = validator.validate( car, Default.class, CarChecks.class, DriverChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testValidatePropertyDriveAway() {
		// create a car and check that everything is ok with it.
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty( car, "manufacturer" );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validateProperty( car, "licensePlate" );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validateProperty( car, "seatCount" );
		assertNumberOfViolations( constraintViolations, 0 );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validateProperty( car, "passedVehicleInspection", CarChecks.class );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"The car has to pass the vehicle inspection first"
		);

		// let's go to the vehicle inspection
		car.setPassedVehicleInspection( true );
		constraintViolations = validator.validateProperty( car, "passedVehicleInspection", CarChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );

		// now let's add a driver. He is 18, but has not passed the driving test yet
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		car.setDriver( john );

		constraintViolations = validator.validateProperty( car, "driver.name", CarChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validateProperty( car, "driver.age", CarChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validateProperty( car, "driver.hasDrivingLicense", DriverChecks.class );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"You first have to pass the driving test"
		);

		// ok, John passes the test
		john.passedDrivingTest( true );
		constraintViolations = validator.validateProperty( car, "driver.hasDrivingLicense", DriverChecks.class );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testOrderedChecks() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		car.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		car.setDriver( john );

		assertNumberOfViolations( validator.validate( car, OrderedChecks.class ), 0 );
	}

	@Test
	public void testValidatePropertyOrderedChecks() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		car.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		car.setDriver( john );

		assertNumberOfViolations( validator.validateProperty( car, "manufacturer", OrderedChecks.class ), 0 );
		assertNumberOfViolations( validator.validateProperty( car, "licensePlate", OrderedChecks.class ), 0 );
		assertNumberOfViolations(
				validator.validateProperty( car, "passedVehicleInspection", OrderedChecks.class ), 0
		);
		assertNumberOfViolations( validator.validateProperty( car, "seatCount", OrderedChecks.class ), 0 );
		assertNumberOfViolations( validator.validateProperty( car, "driver.name", OrderedChecks.class ), 0 );
		assertNumberOfViolations( validator.validateProperty( car, "driver.age", OrderedChecks.class ), 0 );
		assertNumberOfViolations(
				validator.validateProperty( car, "driver.hasDrivingLicense", OrderedChecks.class ), 0
		);
	}

	@Test
	public void testOrderedChecksWithRedefinedDefault() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 2 );
		rentalCar.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		rentalCar.setDriver( john );

		assertNumberOfViolations( validator.validate( rentalCar, Default.class, DriverChecks.class ), 0 );
	}

	@Test
	public void testValidatePropertyOrderedChecksWithRedefinedDefault() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 2 );
		rentalCar.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		rentalCar.setDriver( john );

		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "manufacturer", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "licensePlate", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "passedVehicleInspection", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "seatCount", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "driver.name", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "driver.age", Default.class, DriverChecks.class
				), 0
		);
		assertNumberOfViolations(
				validator.validateProperty(
						rentalCar, "driver.hasDrivingLicense", Default.class, DriverChecks.class
				), 0
		);
	}

	@Test
	public void testOrderedChecksFailsFast() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 0 );

		// this should not create a violation exception due to the 0 seat count failing first due to the GroupSequence on RentalCar
		rentalCar.setPassedVehicleInspection( false );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		rentalCar.setDriver( john );

		Set<ConstraintViolation<RentalCar>> constraintViolations = validator.validate( rentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );

		rentalCar.setSeatCount( 4 );
		constraintViolations = validator.validate( rentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "passedVehicleInspection" );
	}

	@Test
	public void testValidatePropertyOrderedChecksFailsFast() {
		BigRentalCar bigRentalCar = new BigRentalCar( "Morris", "DD-AB-123", 0 );

		// this should cause only one constraint violation due to the GroupSequence on BigRentalCar
		Set<ConstraintViolation<BigRentalCar>> constraintViolations = validator.validateProperty(
				bigRentalCar, "seatCount"
		);

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );
		assertCorrectConstraintViolationMessages( constraintViolations, "must be greater than or equal to 2" );
	}

	@Test
	public void testSubclassesInheritGroupSequence() {
		// our assertion here is based around Item C from Section 3.4.5 of the JSR 303 Validation Spec that class X
		// (MiniRentalCar) without explicitly defining a Default group would then inherit it's super class "Default"
		// constraints along with it's own attribute level constraints not explicitly tied to a group other than Default.
		MiniRentalCar miniRentalCar = new MiniRentalCar( "Morris", "DD-AB-123", 0 );

		// this should not create a violation exception due to the 0 seat count.
		miniRentalCar.setPassedVehicleInspection( false );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		miniRentalCar.setDriver( john );

		Set<ConstraintViolation<MiniRentalCar>> constraintViolations = validator.validate( miniRentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );

		miniRentalCar.setSeatCount( 4 );
		constraintViolations = validator.validate( miniRentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "passedVehicleInspection" );
	}

	@Test
	public void testValidatePropertySubclassesInheritGroupSequence() {
		MiniRentalCar miniRentalCar = new MiniRentalCar( "Morris", "DD-AB-123", 3 );
		miniRentalCar.setPassedVehicleInspection( false );

		// this should cause a violation exception due to the default group sequence redefined on the Rental car class
		Set<ConstraintViolation<MiniRentalCar>> constraintViolations = validator.validateProperty(
				miniRentalCar, "passedVehicleInspection"
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "passedVehicleInspection" );
	}

	@Test
	public void testExplicitGroupSequenceOnSubclass() {
		// with the testSubclassesInheritGroupSequence test failing, we then try a similar test case whereby we
		// explicitly set the Default group for this class.
		AnotherMiniRentalCar anotherMiniRentalCar = new AnotherMiniRentalCar( "Morris", "DD-AB-123", 0 );

		// this should not create a violation exception due to the 0 seat count.
		anotherMiniRentalCar.setPassedVehicleInspection( false );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		anotherMiniRentalCar.setDriver( john );

		Set<ConstraintViolation<AnotherMiniRentalCar>> constraintViolations = validator.validate( anotherMiniRentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );

		anotherMiniRentalCar.setSeatCount( 6 );
		constraintViolations = validator.validateProperty( anotherMiniRentalCar, "seatCount" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );
	}

	@Test
	public void testValidatePropertyExplicitGroupSequenceOnSubclass() {
		AnotherMiniRentalCar anotherMiniRentalCar = new AnotherMiniRentalCar( "Morris", "DD-AB-123", 0 );

		// this should cause only one constraint violation due to the default group sequence redefined on the AnotherMiniRentalCar class.
		Set<ConstraintViolation<AnotherMiniRentalCar>> constraintViolations = validator.validate( anotherMiniRentalCar );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );

		constraintViolations = validator.validateProperty( anotherMiniRentalCar, "seatCount" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "seatCount" );
	}

	private class MiniRentalCar extends RentalCar {
		MiniRentalCar(String manufacturer, String licencePlate, int seatCount) {
			super( manufacturer, licencePlate, seatCount );
		}

		@Override
		@Max(value = 4)
		public int getSeatCount() {
			return super.getSeatCount();
		}
	}

	@GroupSequence( { AnotherMiniRentalCar.class, CarChecks.class })
	private class AnotherMiniRentalCar extends RentalCar {
		AnotherMiniRentalCar(String manufacturer, String licencePlate, int seatCount) {
			super( manufacturer, licencePlate, seatCount );
		}

		@Override
		@Max(value = 4, groups = CarChecks.class)
		public int getSeatCount() {
			return super.getSeatCount();
		}
	}

	@GroupSequence( { BigRentalCar.class, CarChecks.class })
	private class BigRentalCar extends RentalCar {
		BigRentalCar(String manufacturer, String licencePlate, int seatCount) {
			super( manufacturer, licencePlate, seatCount );
		}

		@Override
		@Min(value = 4, groups = CarChecks.class)
		public int getSeatCount() {
			return super.getSeatCount();
		}
	}
}
