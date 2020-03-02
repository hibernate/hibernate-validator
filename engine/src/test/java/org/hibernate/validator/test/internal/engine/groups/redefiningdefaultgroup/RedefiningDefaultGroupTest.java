/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupSequence;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.groups.Default;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
		assertNoViolations( constraintViolations );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validate( car, CarChecks.class );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class )
						.withProperty( "passedVehicleInspection" )
						.withMessage( "The car has to pass the vehicle inspection first" )
		);

		// let's go to the vehicle inspection
		car.setPassedVehicleInspection( true );
		constraintViolations = validator.validate( car );
		assertNoViolations( constraintViolations );

		// now let's add a driver. He is 18, but has not passed the driving test yet
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		car.setDriver( john );
		constraintViolations = validator.validate( car, DriverChecks.class );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withMessage( "You first have to pass the driving test" )
		);

		// ok, John passes the test
		john.passedDrivingTest( true );
		constraintViolations = validator.validate( car, DriverChecks.class );
		assertNoViolations( constraintViolations );

		// just checking that everything is in order now
		constraintViolations = validator.validate( car, Default.class, CarChecks.class, DriverChecks.class );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testValidatePropertyDriveAway() {
		// create a car and check that everything is ok with it.
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty( car, "manufacturer" );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validateProperty( car, "licensePlate" );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validateProperty( car, "seatCount" );
		assertNoViolations( constraintViolations );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validateProperty( car, "passedVehicleInspection", CarChecks.class );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class )
						.withProperty( "passedVehicleInspection" )
						.withMessage( "The car has to pass the vehicle inspection first" )
		);

		// let's go to the vehicle inspection
		car.setPassedVehicleInspection( true );
		constraintViolations = validator.validateProperty( car, "passedVehicleInspection", CarChecks.class );
		assertNoViolations( constraintViolations );

		// now let's add a driver. He is 18, but has not passed the driving test yet
		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		car.setDriver( john );

		constraintViolations = validator.validateProperty( car, "driver.name", CarChecks.class );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validateProperty( car, "driver.age", CarChecks.class );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validateProperty( car, "driver.hasDrivingLicense", DriverChecks.class );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withMessage( "You first have to pass the driving test" )
		);

		// ok, John passes the test
		john.passedDrivingTest( true );
		constraintViolations = validator.validateProperty( car, "driver.hasDrivingLicense", DriverChecks.class );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testOrderedChecks() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		car.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		car.setDriver( john );

		assertNoViolations( validator.validate( car, OrderedChecks.class ) );
	}

	@Test
	public void testValidatePropertyOrderedChecks() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		car.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		car.setDriver( john );

		assertNoViolations( validator.validateProperty( car, "manufacturer", OrderedChecks.class ) );
		assertNoViolations( validator.validateProperty( car, "licensePlate", OrderedChecks.class ) );
		assertNoViolations(
				validator.validateProperty( car, "passedVehicleInspection", OrderedChecks.class )
		);
		assertNoViolations( validator.validateProperty( car, "seatCount", OrderedChecks.class ) );
		assertNoViolations( validator.validateProperty( car, "driver.name", OrderedChecks.class ) );
		assertNoViolations( validator.validateProperty( car, "driver.age", OrderedChecks.class ) );
		assertNoViolations(
				validator.validateProperty( car, "driver.hasDrivingLicense", OrderedChecks.class )
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

		assertNoViolations( validator.validate( rentalCar, Default.class, DriverChecks.class ) );
	}

	@Test
	public void testValidatePropertyOrderedChecksWithRedefinedDefault() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 2 );
		rentalCar.setPassedVehicleInspection( true );

		Driver john = new Driver( "John Doe" );
		john.setAge( 18 );
		john.passedDrivingTest( true );
		rentalCar.setDriver( john );

		assertNoViolations(
				validator.validateProperty(
						rentalCar, "manufacturer", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "licensePlate", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "passedVehicleInspection", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "seatCount", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "driver.name", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "driver.age", Default.class, DriverChecks.class
				)
		);
		assertNoViolations(
				validator.validateProperty(
						rentalCar, "driver.hasDrivingLicense", Default.class, DriverChecks.class
				)
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
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);

		rentalCar.setSeatCount( 4 );
		constraintViolations = validator.validate( rentalCar );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class )
						.withProperty( "passedVehicleInspection" )
						.withMessage( "The car has to pass the vehicle inspection first" )
		);
	}

	@Test
	public void testValidatePropertyOrderedChecksFailsFast() {
		BigRentalCar bigRentalCar = new BigRentalCar( "Morris", "DD-AB-123", 0 );

		// this should cause only one constraint violation due to the GroupSequence on BigRentalCar
		Set<ConstraintViolation<BigRentalCar>> constraintViolations = validator.validateProperty(
				bigRentalCar, "seatCount"
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);
	}

	@Test
	public void testSubclassesInheritGroupSequence() {
		// our assertion here is based around Item C from Section 3.4.5 of the JSR 380 Validation Spec that class X
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
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);

		miniRentalCar.setSeatCount( 4 );
		constraintViolations = validator.validate( miniRentalCar );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class )
						.withProperty( "passedVehicleInspection" )
						.withMessage( "The car has to pass the vehicle inspection first" )
		);
	}

	@Test
	public void testValidatePropertySubclassesInheritGroupSequence() {
		MiniRentalCar miniRentalCar = new MiniRentalCar( "Morris", "DD-AB-123", 3 );
		miniRentalCar.setPassedVehicleInspection( false );

		// this should cause a violation exception due to the default group sequence redefined on the Rental car class
		Set<ConstraintViolation<MiniRentalCar>> constraintViolations = validator.validateProperty(
				miniRentalCar, "passedVehicleInspection"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AssertTrue.class )
						.withProperty( "passedVehicleInspection" )
						.withMessage( "The car has to pass the vehicle inspection first" )
		);
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
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);

		anotherMiniRentalCar.setSeatCount( 6 );
		constraintViolations = validator.validateProperty( anotherMiniRentalCar, "seatCount" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class )
						.withProperty( "seatCount" )
						.withMessage( "must be less than or equal to 4" )
		);
	}

	@Test
	public void testValidatePropertyExplicitGroupSequenceOnSubclass() {
		AnotherMiniRentalCar anotherMiniRentalCar = new AnotherMiniRentalCar( "Morris", "DD-AB-123", 0 );

		// this should cause only one constraint violation due to the default group sequence redefined on the AnotherMiniRentalCar class.
		Set<ConstraintViolation<AnotherMiniRentalCar>> constraintViolations = validator.validate( anotherMiniRentalCar );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);

		constraintViolations = validator.validateProperty( anotherMiniRentalCar, "seatCount" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withProperty( "seatCount" )
						.withMessage( "must be greater than or equal to 2" )
		);
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
