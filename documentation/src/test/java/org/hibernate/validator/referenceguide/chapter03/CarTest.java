package org.hibernate.validator.referenceguide.chapter03;

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
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testLicensePlateNotUpperCase() {

		Car car = new Car( "Morris", "dd-ab-123", 4 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );
		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"Case mode must be UPPER.",
				constraintViolations.iterator().next().getMessage()
		);
	}

	@Test
	public void carIsValid() {

		Car car = new Car( "Morris", "DD-AB-123", 4 );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 0, constraintViolations.size() );
	}
}
