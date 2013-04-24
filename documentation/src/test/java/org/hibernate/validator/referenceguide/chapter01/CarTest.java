package org.hibernate.validator.referenceguide.chapter01;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void manufacturerIsNull() {
		Car car = new Car( null, "DD-AB-123", 4 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void licensePlateTooShort() {
		Car car = new Car( "Morris", "D", 4 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"size must be between 2 and 14",
				constraintViolations.iterator().next().getMessage()
		);
	}

	@Test
	public void seatCountTooLow() {
		Car car = new Car( "Morris", "DD-AB-123", 1 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"must be greater than or equal to 2",
				constraintViolations.iterator().next().getMessage()
		);
	}

	@Test
	public void carIsValid() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 0, constraintViolations.size() );
	}
}
