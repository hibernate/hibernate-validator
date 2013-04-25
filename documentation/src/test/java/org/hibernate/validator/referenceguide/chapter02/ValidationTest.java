package org.hibernate.validator.referenceguide.chapter02;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validator.referenceguide.chapter02.propertylevel.Car;

import static org.junit.Assert.assertEquals;

public class ValidationTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validate() {
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void validateProperty() {
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty(
				car,
				"manufacturer"
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void validateValue() {
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateValue(
				Car.class,
				"manufacturer",
				null
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}
}
