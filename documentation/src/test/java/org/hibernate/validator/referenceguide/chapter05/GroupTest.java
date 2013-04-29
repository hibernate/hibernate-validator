package org.hibernate.validator.referenceguide.chapter05;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GroupTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void driveAway() {
		// create a car and check that everything is ok with it.
		Car car = new Car( "Morris", "DD-AB-123", 2 );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals( 0, constraintViolations.size() );

		// but has it passed the vehicle inspection?
		constraintViolations = validator.validate( car, CarChecks.class );
		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"The car has to pass the vehicle inspection first",
				constraintViolations.iterator().next().getMessage()
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
		assertEquals(
				"You first have to pass the driving test",
				constraintViolations.iterator().next().getMessage()
		);

		// ok, John passes the test
		john.passedDrivingTest( true );
		assertEquals( 0, validator.validate( car, DriverChecks.class ).size() );

		// just checking that everything is in order now
		assertEquals(
				0, validator.validate(
				car,
				Default.class,
				CarChecks.class,
				DriverChecks.class
		).size()
		);
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

	/**
	 * Validating the default group leads to validation on the default group sequence of {@code RentalCar}.
	 */
	@Test
	public void carIsRented() {
		RentalCar rentalCar = new RentalCar( "Morris", "DD-AB-123", 2 );
		rentalCar.setPassedVehicleInspection( true );
		rentalCar.setRented( true );

		Set<ConstraintViolation<RentalCar>> constraintViolations = validator.validate( rentalCar );

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"Wrong message",
				"The car is currently rented out",
				constraintViolations.iterator().next().getMessage()
		);

		rentalCar.setRented( false );
		constraintViolations = validator.validate( rentalCar );

		assertEquals( 0, constraintViolations.size() );
	}
}
